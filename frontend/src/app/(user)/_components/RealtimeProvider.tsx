"use client";

import { Client } from "@stomp/stompjs";
import { createContext, useCallback, useContext, useEffect, useMemo, useRef, useState, type ReactNode } from "react";
import type { AppNotification } from "@/app/_lib/types/notification";
import type { UserSummary } from "@/app/_lib/types/user";

/** 서버가 /user/queue/dm 으로 보내 주는 메시지 */
export type DmSocketMessage = {
  id: number;
  roomId: number;
  senderId: number;
  /** 보낸 회원. 새 DM 알림에 닉네임을 띄울 때 쓴다 */
  sender: UserSummary | null;
  receiverId: number;
  content: string;
  createdAt: string;
};

/** 상대가 내 메시지를 어디까지 읽었는지 (/user/queue/dm-read) */
export type DmReadEvent = {
  readerId: number;
  lastReadMessageId: number;
};

export type RealtimeStatus = "mock" | "connecting" | "connected" | "disconnected";

/** 로그인한 회원 정보. RealtimeSession이 서버에서 확인해 넣어 준다 */
export type RealtimeMe = {
  id: number;
  /** 설정의 DM 알림 켜짐 여부 */
  notifyDm: boolean;
};

type Events = {
  dm: DmSocketMessage;
  "dm-read": DmReadEvent;
  notification: AppNotification;
};

type Listener<K extends keyof Events> = (event: Events[K]) => void;

const queues: Record<keyof Events, string> = {
  dm: "/user/queue/dm",
  "dm-read": "/user/queue/dm-read",
  notification: "/user/queue/notifications",
};

type Realtime = {
  me: RealtimeMe | null;
  status: RealtimeStatus;
  error: string | null;
  setMe: (me: RealtimeMe | null) => void;
  /** 보내기에 성공하면 true. 연결돼 있지 않으면 false */
  publish: (destination: string, body: unknown) => boolean;
  subscribe: <K extends keyof Events>(kind: K, listener: Listener<K>) => () => void;
};

// 백엔드 STOMP 주소. 비워 두면 목업처럼 화면에만 추가한다.
// 백엔드 context-path(/api/v1) 아래에 있다.
// 예: ws://localhost:8080/api/v1/ws(로컬), /api/v1/ws(Docker·배포: 접속한 주소 기준으로 ws://·wss://를 붙인다)
const WS_URL = process.env.NEXT_PUBLIC_WS_URL;

function resolveWsUrl(url: string) {
  if (!url.startsWith("/")) return url;
  const scheme = window.location.protocol === "https:" ? "wss" : "ws";
  return `${scheme}://${window.location.host}${url}`;
}

const RealtimeContext = createContext<Realtime | null>(null);

function useRealtimeContext() {
  const value = useContext(RealtimeContext);
  if (!value) throw new Error("RealtimeProvider 안에서만 쓸 수 있어요.");
  return value;
}

export function useRealtime() {
  return useRealtimeContext();
}

/** 실시간 이벤트를 받는다. handler는 항상 최신 값을 부른다. */
export function useRealtimeEvent<K extends keyof Events>(kind: K, handler: Listener<K>) {
  const { subscribe } = useRealtimeContext();
  const handlerRef = useRef(handler);

  useEffect(() => {
    handlerRef.current = handler;
  }, [handler]);

  useEffect(() => subscribe(kind, (event) => handlerRef.current(event)), [kind, subscribe]);
}

type Props = {
  children: ReactNode;
};

/**
 * 로그인한 동안 실시간 연결(STOMP) 하나를 유지하고 DM·읽음·알림을 나눠 준다.
 * DM 창을 닫아도 연결은 그대로라, 새 DM 알림과 알림 종이 새로고침 없이 갱신된다.
 * 로그인은 브라우저가 연결 요청에 실어 보내는 access_token 쿠키로 확인한다.
 */
export function RealtimeProvider({ children }: Props) {
  const [me, setMeState] = useState<RealtimeMe | null>(null);
  const [status, setStatus] = useState<RealtimeStatus>(WS_URL ? "connecting" : "mock");
  const [error, setError] = useState<string | null>(null);
  const clientRef = useRef<Client | null>(null);
  const listenersRef = useRef(new Map<keyof Events, Set<Listener<keyof Events>>>());
  const myId = me?.id ?? null;

  const setMe = useCallback((next: RealtimeMe | null) => {
    setMeState((current) =>
      current?.id === next?.id && current?.notifyDm === next?.notifyDm ? current : next,
    );
  }, []);

  useEffect(() => {
    if (!WS_URL || myId === null) return;

    const client = new Client({
      brokerURL: resolveWsUrl(WS_URL),
      reconnectDelay: 3000,
      heartbeatIncoming: 10000,
      heartbeatOutgoing: 10000,
      onConnect: () => {
        setStatus("connected");
        for (const kind of Object.keys(queues) as (keyof Events)[]) {
          client.subscribe(queues[kind], (frame) => {
            const event = JSON.parse(frame.body);
            listenersRef.current.get(kind)?.forEach((listener) => listener(event));
          });
        }
        client.subscribe("/user/queue/errors", (frame) => setError(JSON.parse(frame.body).message));
      },
      onWebSocketClose: () => setStatus("disconnected"),
      onStompError: (frame) => setError(frame.headers.message ?? "실시간 연결에 실패했어요."),
    });
    client.activate();
    clientRef.current = client;

    return () => {
      clientRef.current = null;
      setStatus("connecting");
      void client.deactivate();
    };
  }, [myId]);

  const publish = useCallback((destination: string, body: unknown) => {
    const client = clientRef.current;
    if (!client?.connected) return false;
    setError(null);
    client.publish({ destination, body: JSON.stringify(body) });
    return true;
  }, []);

  const subscribe = useCallback(<K extends keyof Events>(kind: K, listener: Listener<K>) => {
    const listeners = listenersRef.current;
    const set = listeners.get(kind) ?? new Set();
    set.add(listener as Listener<keyof Events>);
    listeners.set(kind, set);
    return () => {
      set.delete(listener as Listener<keyof Events>);
    };
  }, []);

  const value = useMemo<Realtime>(
    () => ({ me, status, error, setMe, publish, subscribe }),
    [me, status, error, setMe, publish, subscribe],
  );

  return <RealtimeContext value={value}>{children}</RealtimeContext>;
}

/** 서버에서 확인한 로그인 정보를 실시간 연결에 넘긴다. 화면에는 아무것도 그리지 않는다. */
export function RealtimeLogin({ me }: { me: RealtimeMe }) {
  const { setMe } = useRealtimeContext();
  useEffect(() => {
    setMe(me);
  }, [me, setMe]);
  // 로그아웃해서 사라지면 연결을 끊는다.
  useEffect(() => () => setMe(null), [setMe]);
  return null;
}
