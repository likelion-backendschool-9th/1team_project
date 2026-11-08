"use client";

import { Client } from "@stomp/stompjs";
import { useEffect, useRef, useState } from "react";

/** 서버가 /user/queue/dm 으로 보내 주는 메시지 */
export type DmSocketMessage = {
  id: number;
  roomId: number;
  senderId: number;
  receiverId: number;
  content: string;
  createdAt: string;
};

export type DmSocketStatus = "mock" | "connecting" | "connected" | "disconnected";

// 백엔드 STOMP 주소. 비워 두면 목업처럼 화면에만 추가한다.
// 백엔드 context-path(/api/v1) 아래에 있다.
// 예: ws://localhost:8080/api/v1/ws(로컬), /api/v1/ws(Docker·배포: 접속한 주소 기준으로 ws://·wss://를 붙인다)
const WS_URL = process.env.NEXT_PUBLIC_WS_URL;

function resolveWsUrl(url: string) {
  if (!url.startsWith("/")) return url;
  const scheme = window.location.protocol === "https:" ? "wss" : "ws";
  return `${scheme}://${window.location.host}${url}`;
}

// DM 실시간 연결. DM 창이 열려 있는 동안만 연결한다. 로그인은 브라우저가 연결 요청에 실어 보내는 access_token 쿠키로 확인한다.
export function useDmSocket(onMessage: (message: DmSocketMessage) => void) {
  const [status, setStatus] = useState<DmSocketStatus>(WS_URL ? "connecting" : "mock");
  const [error, setError] = useState<string | null>(null);
  const clientRef = useRef<Client | null>(null);
  // 구독 콜백이 항상 최신 onMessage를 부르도록 ref에 둔다.
  const onMessageRef = useRef(onMessage);

  useEffect(() => {
    onMessageRef.current = onMessage;
  }, [onMessage]);

  useEffect(() => {
    if (!WS_URL) return;

    const client = new Client({
      brokerURL: resolveWsUrl(WS_URL),
      reconnectDelay: 3000,
      heartbeatIncoming: 10000,
      heartbeatOutgoing: 10000,
      onConnect: () => {
        setStatus("connected");
        client.subscribe("/user/queue/dm", (frame) => onMessageRef.current(JSON.parse(frame.body)));
        client.subscribe("/user/queue/errors", (frame) => setError(JSON.parse(frame.body).message));
      },
      onWebSocketClose: () => setStatus("disconnected"),
      onStompError: (frame) => setError(frame.headers.message ?? "실시간 연결에 실패했어요."),
    });
    client.activate();
    clientRef.current = client;

    return () => {
      clientRef.current = null;
      void client.deactivate();
    };
  }, []);

  /** 보내기에 성공하면 true. 연결돼 있지 않으면 false */
  function send(receiverId: number, content: string): boolean {
    const client = clientRef.current;
    if (!client?.connected) return false;
    setError(null);
    client.publish({ destination: "/app/dm.send", body: JSON.stringify({ receiverId, content }) });
    return true;
  }

  return { status, error, send };
}
