import assert from "node:assert/strict";
import { describe, it } from "node:test";
import { checkBackendFields, checkBackendUrl } from "./rules.ts";

describe("외부 백엔드 주소 안내", () => {
  for (const url of ["https://example.com/api", "HTTPS://Example.com", "  https://example.com:8443/v1  "]) {
    it(`허용: ${url}`, () => assert.equal(checkBackendUrl(url), null));
  }

  for (const url of [
    "", "   ", "http://example.com", "javascript:alert(1)", "example.com", "https://",
    "https://user:pw@example.com", "https://example.com/a?token=1", "https://example.com/#x",
    `https://example.com/${"a".repeat(500)}`,
  ]) {
    it(`거부: ${url.slice(0, 40) || "(빈 값)"}`, () => assert.ok(checkBackendUrl(url)));
  }
});

describe("입력 필드 검사", () => {
  it("상태 확인 주소는 선택이다", () => {
    assert.deepEqual(checkBackendFields("https://example.com/api", "  "), {});
  });

  it("API 주소가 없으면 오류다", () => {
    assert.ok(checkBackendFields("", "").apiBaseUrl);
  });

  it("상태 확인 주소를 넣었으면 검사한다", () => {
    assert.ok(checkBackendFields("https://example.com", "http://example.com/health").healthCheckUrl);
  });
});
