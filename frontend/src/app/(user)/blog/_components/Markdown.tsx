import type { ReactNode } from "react";
import styles from "./Markdown.module.css";

type Block =
  | { kind: "heading"; level: 3 | 4 | 5; text: string }
  | { kind: "p"; lines: string[] }
  | { kind: "ul" | "ol"; items: string[]; start: number }
  | { kind: "quote"; lines: string[] }
  | { kind: "code"; lang: string; text: string }
  | { kind: "table"; head: string[]; rows: string[][] }
  | { kind: "hr" };

const ORDERED = /^\s*(\d+)[.)]\s+(.*)$/;
const UNORDERED = /^\s*[-*+]\s+(.*)$/;
const HEADING = /^(#{1,6})\s+(.*)$/;
const HR = /^\s*([-*_])(\s*\1){2,}\s*$/;
const TABLE_SEPARATOR = /^\s*\|?\s*:?-{3,}:?\s*(\|\s*:?-{3,}:?\s*)*\|?\s*$/;

function splitRow(line: string): string[] {
  return line
    .trim()
    .replace(/^\|/, "")
    .replace(/\|$/, "")
    .split("|")
    .map((cell) => cell.trim());
}

// 글 본문 마크다운을 블록 단위로 해석한다.
// 제목(#~######), 문단, 순서 있는/없는 목록, 인용, 코드 블록, 표, 구분선을 지원한다.
function parse(source: string): Block[] {
  const blocks: Block[] = [];
  const lines = source.replace(/\r\n?/g, "\n").split("\n");
  for (let i = 0; i < lines.length; i += 1) {
    const line = lines[i];
    if (line.trimStart().startsWith("```")) {
      const code: string[] = [];
      const lang = line.trimStart().slice(3).trim();
      for (i += 1; i < lines.length && !lines[i].trimStart().startsWith("```"); i += 1) code.push(lines[i]);
      blocks.push({ kind: "code", lang, text: code.join("\n") });
      continue;
    }
    if (!line.trim()) continue;

    const heading = HEADING.exec(line);
    if (heading) {
      // 글 제목이 h2라서 본문 제목은 h3부터 쓴다. (# · ## → h3, ### → h4, 그 아래 → h5)
      const depth = heading[1].length;
      blocks.push({ kind: "heading", level: depth <= 2 ? 3 : depth === 3 ? 4 : 5, text: heading[2] });
      continue;
    }
    if (HR.test(line)) {
      blocks.push({ kind: "hr" });
      continue;
    }
    if (line.includes("|") && i + 1 < lines.length && TABLE_SEPARATOR.test(lines[i + 1])) {
      const head = splitRow(line);
      const rows: string[][] = [];
      for (i += 2; i < lines.length && lines[i].includes("|") && lines[i].trim(); i += 1) rows.push(splitRow(lines[i]));
      i -= 1;
      blocks.push({ kind: "table", head, rows });
      continue;
    }
    if (line.trimStart().startsWith(">")) {
      const quote: string[] = [];
      for (; i < lines.length && lines[i].trimStart().startsWith(">"); i += 1) quote.push(lines[i].trimStart().replace(/^>\s?/, ""));
      i -= 1;
      blocks.push({ kind: "quote", lines: quote });
      continue;
    }
    const unordered = UNORDERED.exec(line);
    const ordered = ORDERED.exec(line);
    if (unordered || ordered) {
      const kind = unordered ? "ul" : "ol";
      const last = blocks.at(-1);
      const text = (unordered ?? ordered)![unordered ? 1 : 2];
      if (last && last.kind === kind) last.items.push(text);
      else blocks.push({ kind, items: [text], start: ordered ? Number(ordered[1]) : 1 });
      continue;
    }

    // 이어진 줄은 한 문단으로 묶고 줄바꿈은 그대로 살린다.
    const last = blocks.at(-1);
    const previous = lines[i - 1];
    if (last?.kind === "p" && previous !== undefined && previous.trim()) last.lines.push(line);
    else blocks.push({ kind: "p", lines: [line] });
  }
  return blocks;
}

const INLINE =
  /(`[^`]+`)|(!\[[^\]]*\]\([^)\s]+\))|(\[[^\]]+\]\([^)\s]+\))|(\*\*[^*]+?\*\*|__[^_]+?__)|(~~[^~]+?~~)|(\*[^*\s][^*]*?\*|(?<![\w])_[^_\s][^_]*?_(?![\w]))|(https?:\/\/[^\s<]+[^\s<.,:;"')\]])/g;

// javascript: 같은 위험한 주소는 막고 http(s)·사이트 안 경로·메일만 링크로 만든다.
function safeUrl(url: string): string | null {
  return /^(https?:\/\/|\/(?!\/)|#|mailto:)/i.test(url) ? url : null;
}

function isExternal(url: string): boolean {
  return /^https?:\/\//i.test(url);
}

function renderLink(href: string, children: ReactNode, key: string): ReactNode {
  const url = safeUrl(href);
  if (!url) return <span key={key}>{children}</span>;
  return isExternal(url) ? (
    <a key={key} href={url} target="_blank" rel="noopener noreferrer nofollow">
      {children}
    </a>
  ) : (
    <a key={key} href={url}>
      {children}
    </a>
  );
}

// 굵게, 기울임, 취소선, 인라인 코드, 링크, 이미지, 주소 자동 링크를 React 요소로 바꾼다. (HTML 문자열을 넣지 않는다)
function inline(text: string, prefix: string): ReactNode[] {
  const nodes: ReactNode[] = [];
  let cursor = 0;
  let index = 0;
  for (const match of text.matchAll(INLINE)) {
    const start = match.index ?? 0;
    if (start > cursor) nodes.push(text.slice(cursor, start));
    const token = match[0];
    const key = `${prefix}-${index}`;
    index += 1;
    if (match[1]) {
      nodes.push(<code key={key}>{token.slice(1, -1)}</code>);
    } else if (match[2]) {
      const [, alt, src] = /^!\[([^\]]*)\]\(([^)\s]+)\)$/.exec(token)!;
      const url = safeUrl(src);
      // 글쓴이가 넣은 외부 이미지라 크기를 미리 알 수 없어 next/image 대신 img를 쓴다.
      // eslint-disable-next-line @next/next/no-img-element
      nodes.push(url ? <img key={key} src={url} alt={alt} loading="lazy" /> : alt);
    } else if (match[3]) {
      const [, label, href] = /^\[([^\]]+)\]\(([^)\s]+)\)$/.exec(token)!;
      nodes.push(renderLink(href, inline(label, key), key));
    } else if (match[4]) {
      nodes.push(<strong key={key}>{inline(token.slice(2, -2), key)}</strong>);
    } else if (match[5]) {
      nodes.push(<del key={key}>{inline(token.slice(2, -2), key)}</del>);
    } else if (match[6]) {
      nodes.push(<em key={key}>{inline(token.slice(1, -1), key)}</em>);
    } else {
      nodes.push(renderLink(token, token, key));
    }
    cursor = start + token.length;
  }
  if (cursor < text.length) nodes.push(text.slice(cursor));
  return nodes;
}

function renderLines(lines: string[], key: string): ReactNode[] {
  return lines.flatMap((line, index) => (index === 0 ? inline(line, `${key}-${index}`) : [<br key={`${key}-br-${index}`} />, ...inline(line, `${key}-${index}`)]));
}

type Props = {
  source: string;
  limit?: number;
};

export function Markdown({ source, limit }: Props) {
  const blocks = parse(source).slice(0, limit);
  return (
    <div className={styles.body}>
      {blocks.map((block, index) => {
        const key = `${block.kind}-${index}`;
        switch (block.kind) {
          case "heading": {
            const Tag = `h${block.level}` as const;
            return <Tag key={key}>{inline(block.text, key)}</Tag>;
          }
          case "ul":
            return (
              <ul key={key}>
                {block.items.map((item, i) => (
                  <li key={`${key}-${i}`}>{inline(item, `${key}-${i}`)}</li>
                ))}
              </ul>
            );
          case "ol":
            return (
              <ol key={key} start={block.start === 1 ? undefined : block.start}>
                {block.items.map((item, i) => (
                  <li key={`${key}-${i}`}>{inline(item, `${key}-${i}`)}</li>
                ))}
              </ol>
            );
          case "quote":
            return <blockquote key={key}>{renderLines(block.lines, key)}</blockquote>;
          case "code":
            return (
              <pre key={key} data-lang={block.lang || undefined}>
                <code>{block.text}</code>
              </pre>
            );
          case "table":
            return (
              <div key={key} className={styles.tableWrap}>
                <table>
                  <thead>
                    <tr>
                      {block.head.map((cell, i) => (
                        <th key={`${key}-h-${i}`}>{inline(cell, `${key}-h-${i}`)}</th>
                      ))}
                    </tr>
                  </thead>
                  <tbody>
                    {block.rows.map((row, r) => (
                      <tr key={`${key}-r-${r}`}>
                        {row.map((cell, c) => (
                          <td key={`${key}-${r}-${c}`}>{inline(cell, `${key}-${r}-${c}`)}</td>
                        ))}
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            );
          case "hr":
            return <hr key={key} />;
          default:
            return <p key={key}>{renderLines(block.lines, key)}</p>;
        }
      })}
    </div>
  );
}
