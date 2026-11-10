// 데모 iframe이 보내는 postMessage를 검증해 화면 상태로 바꾼다.
// 메시지는 데모 코드도 위조할 수 있다. 이 데모를 보는 사람의 안내 문구만 바꿀 뿐 서버 상태나 권한에는 쓰지 않는다.

const MESSAGE_SOURCE = "dipsource-demo";
const MAX_TEXT = 200;
const KINDS = ["script", "promise", "resource", "csp", "app", "http"] as const;

export type DemoErrorKind = (typeof KINDS)[number];

export type DemoMessage =
  | { type: "boot" | "loaded" | "ready" }
  | { type: "error"; kind: DemoErrorKind; message: string; tag: string; status: number | null };

export type FrameEvent = DemoMessage | { type: "timeout" };

export type FrameStatus = {
  phase: "loading" | "running" | "failed";
  booted: boolean;
  reason: string | null;
  warning: string | null;
};

export const initialFrameStatus: FrameStatus = { phase: "loading", booted: false, reason: null, warning: null };

function text(value: unknown): string {
  return typeof value === "string" ? value.slice(0, MAX_TEXT) : "";
}

/** 형식·nonce가 맞지 않으면 null. 모르는 필드는 버린다. */
export function parseDemoMessage(data: unknown, nonce: string): DemoMessage | null {
  if (typeof data !== "object" || data === null) return null;
  const message = data as Record<string, unknown>;
  if (message.source !== MESSAGE_SOURCE || message.nonce !== nonce) return null;

  if (message.type === "boot" || message.type === "loaded" || message.type === "ready") return { type: message.type };
  if (message.type !== "error") return null;

  const detail = (typeof message.detail === "object" && message.detail !== null ? message.detail : {}) as Record<string, unknown>;
  const kind = KINDS.find((item) => item === detail.kind);
  if (!kind) return null;
  return {
    type: "error",
    kind,
    message: text(detail.message),
    tag: text(detail.tag).toUpperCase(),
    status: typeof detail.status === "number" ? detail.status : null,
  };
}

function isFatal(event: Extract<DemoMessage, { type: "error" }>, phase: FrameStatus["phase"]): boolean {
  if (event.kind === "http" || event.kind === "app") return true;
  if (phase === "running") return false;
  if (event.kind === "resource") return event.tag === "SCRIPT" || event.tag === "LINK";
  return event.kind === "script" || event.kind === "promise";
}

function describe(event: Extract<DemoMessage, { type: "error" }>): string {
  switch (event.kind) {
    case "http":
      return event.status !== null && event.status >= 500
        ? "데모 서버에서 오류가 났어요."
        : "데모 파일을 찾지 못했어요. 공개가 내려갔거나 파일이 없을 수 있어요.";
    case "resource":
      return "필요한 파일을 불러오지 못했어요.";
    case "csp":
      return "보안 정책으로 일부 기능(외부 통신 등)이 차단됐어요.";
    case "app":
      return event.message ? `데모가 오류를 알렸어요: ${event.message}` : "데모가 오류를 알렸어요.";
    default:
      return "데모를 시작하는 중 오류가 났어요.";
  }
}

export function nextFrameStatus(state: FrameStatus, event: FrameEvent): FrameStatus {
  if (state.phase === "failed") return state;
  switch (event.type) {
    case "boot":
      return { ...state, booted: true };
    case "loaded":
    case "ready":
      return { ...state, booted: true, phase: "running" };
    case "timeout":
      if (state.phase === "running") return state;
      return {
        ...state,
        phase: "failed",
        reason: state.booted ? "데모 초기화가 너무 오래 걸려요." : "데모가 응답하지 않아요.",
      };
    case "error":
      return isFatal(event, state.phase)
        ? { ...state, phase: "failed", reason: describe(event) }
        : { ...state, warning: describe(event) };
  }
}

/** iframe 한 번 실행마다 새로 만든다. */
export function createNonce(): string {
  const bytes = crypto.getRandomValues(new Uint8Array(16));
  return Array.from(bytes, (byte) => byte.toString(16).padStart(2, "0")).join("");
}
