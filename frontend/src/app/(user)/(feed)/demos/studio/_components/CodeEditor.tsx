"use client";

import { useEffect, useRef } from "react";
import type { Extension } from "@codemirror/state";
import styles from "./DemoStudio.module.css";

export type EditorLanguage = "html" | "css" | "javascript" | "json";

type Props = {
  label: string;
  language: EditorLanguage;
  value: string;
  onChange: (value: string) => void;
};

async function loadLanguage(language: EditorLanguage): Promise<Extension> {
  switch (language) {
    case "html":
      return (await import("@codemirror/lang-html")).html();
    case "css":
      return (await import("@codemirror/lang-css")).css();
    case "json":
      return (await import("@codemirror/lang-json")).json();
    default:
      return (await import("@codemirror/lang-javascript")).javascript();
  }
}

// CodeMirror는 첫 화면 번들에 넣지 않는다. 탭을 바꾸면 key로 새로 마운트되므로 초기값만 넘긴다.
export function CodeEditor({ label, language, value, onChange }: Props) {
  const hostRef = useRef<HTMLDivElement>(null);
  const initialValue = useRef(value);
  const onChangeRef = useRef(onChange);

  useEffect(() => {
    onChangeRef.current = onChange;
  }, [onChange]);

  useEffect(() => {
    let cancelled = false;
    let destroy = () => {};
    (async () => {
      const [{ EditorView, basicSetup }, languageSupport] = await Promise.all([
        import("codemirror"),
        loadLanguage(language),
      ]);
      if (cancelled || !hostRef.current) return;
      const view = new EditorView({
        doc: initialValue.current,
        parent: hostRef.current,
        extensions: [
          basicSetup,
          languageSupport,
          EditorView.lineWrapping,
          EditorView.contentAttributes.of({ "aria-label": label }),
          EditorView.updateListener.of((update) => {
            if (update.docChanged) onChangeRef.current(update.state.doc.toString());
          }),
        ],
      });
      destroy = () => view.destroy();
    })();
    return () => {
      cancelled = true;
      destroy();
    };
  }, [label, language]);

  return <div ref={hostRef} className={styles.editor} />;
}
