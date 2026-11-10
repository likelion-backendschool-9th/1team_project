import assert from "node:assert/strict";
import { deflateRawSync } from "node:zlib";
import { describe, it } from "node:test";
import { planPreview, readZip, resolveLocal } from "./localPreview.ts";

const text = (value) => new TextEncoder().encode(value);

/** 테스트용 최소 ZIP 작성기 */
function zip(files) {
  const locals = [];
  const centrals = [];
  let offset = 0;
  for (const [path, content, method = 8] of files) {
    const name = text(path);
    const data = method === 8 ? deflateRawSync(content) : Buffer.from(content);
    const local = Buffer.alloc(30);
    local.writeUInt32LE(0x04034b50, 0);
    local.writeUInt16LE(method, 8);
    local.writeUInt32LE(data.length, 18);
    local.writeUInt32LE(content.length, 22);
    local.writeUInt16LE(name.length, 26);
    locals.push(local, Buffer.from(name), data);
    const central = Buffer.alloc(46);
    central.writeUInt32LE(0x02014b50, 0);
    central.writeUInt16LE(method, 10);
    central.writeUInt32LE(data.length, 20);
    central.writeUInt32LE(content.length, 24);
    central.writeUInt16LE(name.length, 28);
    central.writeUInt32LE(offset, 42);
    centrals.push(central, Buffer.from(name));
    offset += local.length + name.length + data.length;
  }
  const centralBytes = Buffer.concat(centrals);
  const end = Buffer.alloc(22);
  end.writeUInt32LE(0x06054b50, 0);
  end.writeUInt16LE(files.length, 8);
  end.writeUInt16LE(files.length, 10);
  end.writeUInt32LE(centralBytes.length, 12);
  end.writeUInt32LE(offset, 16);
  const merged = Buffer.concat([...locals, centralBytes, end]);
  return merged.buffer.slice(merged.byteOffset, merged.byteOffset + merged.length);
}

const decode = (bytes) => new TextDecoder().decode(bytes);

describe("readZip", () => {
  it("Deflate·Stored 항목을 푼다", async () => {
    const entries = await readZip(zip([["index.html", text("<p>hi</p>")], ["a.txt", text("plain"), 0]]));
    assert.deepEqual(entries.map((entry) => entry.path), ["index.html", "a.txt"]);
    assert.equal(decode(entries[0].bytes), "<p>hi</p>");
    assert.equal(decode(entries[1].bytes), "plain");
  });

  it("macOS 잔여물과 폴더 항목을 건너뛴다", async () => {
    const entries = await readZip(zip([["dir/", text("")], ["__MACOSX/x", text("y")], [".DS_Store", text("y")], ["index.html", text("a")]]));
    assert.deepEqual(entries.map((entry) => entry.path), ["index.html"]);
  });

  it("압축 폭탄은 실제로 푼 크기로 거부한다", async () => {
    assert.equal(await readZip(zip([["index.html", new Uint8Array(31 * 1024 * 1024)]])), null);
  });

  it("ZIP이 아니면 null", async () => {
    assert.equal(await readZip(text("not a zip file, just text bytes here").buffer), null);
  });
});

describe("planPreview", () => {
  const entry = (path, value) => ({ path, bytes: text(value) });

  it("단순 HTML/CSS/JS는 로컬로 미리 본다", () => {
    const plan = planPreview([entry("index.html", "<script src=a.js></script>"), entry("a.js", "document.title='x'"), entry("a.css", "b{}")]);
    assert.equal(plan.mode, "local");
  });

  it("ES 모듈 번들은 서버 미리보기로 넘긴다", () => {
    assert.equal(planPreview([entry("index.html", '<script type="module" src="./a.js"></script>')]).mode, "server");
  });

  it("동적 import·fetch를 쓰면 서버 미리보기로 넘긴다", () => {
    for (const js of ['import("./x.js")', "import a from './a.js'", "fetch('./data.json')", "new Worker('w.js')"]) {
      assert.equal(planPreview([entry("index.html", "<p>"), entry("a.js", js)]).mode, "server", js);
    }
  });

  it("index.html이 없거나 파일이 많으면 서버로 넘긴다", () => {
    assert.equal(planPreview([entry("a.html", "x")]).mode, "server");
    const many = [entry("index.html", "x"), ...Array.from({ length: 31 }, (_, i) => entry(`f${i}.css`, ""))];
    assert.equal(planPreview(many).mode, "server");
  });
});

describe("resolveLocal", () => {
  it("상대 경로를 ZIP 경로로 바꾼다", () => {
    assert.equal(resolveLocal("index.html", "./assets/a.js"), "assets/a.js");
    assert.equal(resolveLocal("css/a.css", "../img/b.png"), "img/b.png");
    assert.equal(resolveLocal("index.html", "a.js?v=1#x"), "a.js");
  });

  it("외부 주소는 건드리지 않는다", () => {
    for (const reference of ["https://x.test/a.js", "//x.test/a.js", "data:text/plain,a", "#id"]) {
      assert.equal(resolveLocal("index.html", reference), null, reference);
    }
  });

  it("ZIP 밖으로 나가는 경로도 안전하게 접힌다", () => {
    assert.equal(resolveLocal("index.html", "../../etc/passwd"), "etc/passwd");
  });
});
