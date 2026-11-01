import styles from "./Markdown.module.css";

type Block =
  | { kind: "h2"; text: string }
  | { kind: "p"; text: string }
  | { kind: "ul"; items: string[] }
  | { kind: "code"; lang: string; text: string };

// 글 본문에 쓰는 마크다운의 일부(제목, 문단, 목록, 코드 블록)만 해석한다.
function parse(source: string): Block[] {
  const blocks: Block[] = [];
  const lines = source.split("\n");
  for (let i = 0; i < lines.length; i += 1) {
    const line = lines[i];
    if (line.startsWith("```")) {
      const code: string[] = [];
      const lang = line.slice(3).trim();
      for (i += 1; i < lines.length && !lines[i].startsWith("```"); i += 1) code.push(lines[i]);
      blocks.push({ kind: "code", lang, text: code.join("\n") });
    } else if (line.startsWith("## ")) {
      blocks.push({ kind: "h2", text: line.slice(3) });
    } else if (line.startsWith("- ")) {
      const last = blocks.at(-1);
      if (last?.kind === "ul") last.items.push(line.slice(2));
      else blocks.push({ kind: "ul", items: [line.slice(2)] });
    } else if (line.trim()) {
      blocks.push({ kind: "p", text: line });
    }
  }
  return blocks;
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
        if (block.kind === "h2") return <h3 key={key}>{block.text}</h3>;
        if (block.kind === "ul")
          return (
            <ul key={key}>
              {block.items.map((item) => (
                <li key={item}>{item}</li>
              ))}
            </ul>
          );
        if (block.kind === "code")
          return (
            <pre key={key} data-lang={block.lang || undefined}>
              <code>{block.text}</code>
            </pre>
          );
        return <p key={key}>{block.text}</p>;
      })}
    </div>
  );
}
