import assert from "node:assert/strict";
import { describe, it } from "node:test";
import { createNonce, initialFrameStatus, nextFrameStatus, parseDemoMessage } from "./demoFrame.ts";

const NONCE = "n".repeat(32);
const message = (type, detail = null, nonce = NONCE, source = "dipsource-demo") => ({ source, nonce, type, detail });

describe("parseDemoMessage", () => {
  it("nonce와 source가 맞는 메시지만 받는다", () => {
    assert.deepEqual(parseDemoMessage(message("boot"), NONCE), { type: "boot" });
    assert.equal(parseDemoMessage(message("boot", null, "other"), NONCE), null);
    assert.equal(parseDemoMessage(message("boot", null, NONCE, "evil"), NONCE), null);
  });

  it("객체가 아니거나 모르는 타입이면 버린다", () => {
    for (const data of [null, undefined, "boot", 1, [], {}]) assert.equal(parseDemoMessage(data, NONCE), null);
    assert.equal(parseDemoMessage(message("navigate"), NONCE), null);
    assert.equal(parseDemoMessage(message("error", { kind: "steal" }), NONCE), null);
  });

  it("문자열 길이를 자르고 잘못된 타입의 필드는 비운다", () => {
    const parsed = parseDemoMessage(message("error", { kind: "script", message: "x".repeat(999), tag: 3, status: "500" }), NONCE);
    assert.equal(parsed.message.length, 200);
    assert.equal(parsed.tag, "");
    assert.equal(parsed.status, null);
  });

  it("추가 필드를 결과에 옮기지 않는다", () => {
    const parsed = parseDemoMessage({ ...message("ready"), redirect: "https://evil.test" }, NONCE);
    assert.deepEqual(parsed, { type: "ready" });
  });
});

describe("nextFrameStatus", () => {
  const error = (kind, extra = {}) => ({ type: "error", kind, message: "", tag: "", status: null, ...extra });
  const running = nextFrameStatus(initialFrameStatus, { type: "loaded" });

  it("onLoad가 아니라 데모가 보낸 신호로 실행 상태가 된다", () => {
    const booted = nextFrameStatus(initialFrameStatus, { type: "boot" });
    assert.equal(booted.phase, "loading");
    assert.equal(nextFrameStatus(booted, { type: "loaded" }).phase, "running");
    assert.equal(nextFrameStatus(booted, { type: "ready" }).phase, "running");
  });

  it("아무 신호가 없으면 시간 초과로 실패한다", () => {
    assert.equal(nextFrameStatus(initialFrameStatus, { type: "timeout" }).reason, "데모가 응답하지 않아요.");
    const booted = nextFrameStatus(initialFrameStatus, { type: "boot" });
    assert.equal(nextFrameStatus(booted, { type: "timeout" }).reason, "데모 초기화가 너무 오래 걸려요.");
    assert.equal(nextFrameStatus(running, { type: "timeout" }).phase, "running");
  });

  it("404·5xx는 실패다", () => {
    assert.match(nextFrameStatus(initialFrameStatus, error("http", { status: 404 })).reason, /찾지 못했어요/);
    assert.match(nextFrameStatus(initialFrameStatus, error("http", { status: 502 })).reason, /서버에서 오류/);
  });

  it("초기화 전 스크립트 오류와 JS·CSS 누락은 실패다", () => {
    assert.equal(nextFrameStatus(initialFrameStatus, error("script")).phase, "failed");
    assert.equal(nextFrameStatus(initialFrameStatus, error("promise")).phase, "failed");
    assert.equal(nextFrameStatus(initialFrameStatus, error("resource", { tag: "SCRIPT" })).phase, "failed");
    assert.equal(nextFrameStatus(initialFrameStatus, error("resource", { tag: "LINK" })).phase, "failed");
  });

  it("이미지 누락과 보안 정책 위반은 경고만 남긴다", () => {
    const image = nextFrameStatus(initialFrameStatus, error("resource", { tag: "IMG" }));
    assert.equal(image.phase, "loading");
    assert.ok(image.warning);
    assert.match(nextFrameStatus(initialFrameStatus, error("csp")).warning, /보안 정책/);
  });

  it("실행 중 오류는 화면을 막지 않지만 SDK가 알린 오류는 실패다", () => {
    assert.equal(nextFrameStatus(running, error("script")).phase, "running");
    assert.equal(nextFrameStatus(running, error("app", { message: "초기화 실패" })).phase, "failed");
  });

  it("한 번 실패하면 뒤늦은 신호로 되돌아가지 않는다", () => {
    const failed = nextFrameStatus(initialFrameStatus, error("script"));
    assert.equal(nextFrameStatus(failed, { type: "loaded" }).phase, "failed");
  });
});

describe("createNonce", () => {
  it("Worker가 받는 형식이고 매번 다르다", () => {
    assert.match(createNonce(), /^[A-Za-z0-9_-]{16,64}$/);
    assert.notEqual(createNonce(), createNonce());
  });
});
