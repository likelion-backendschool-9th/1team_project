import type { Metadata } from "next";
import Link from "next/link";
import { notFound } from "next/navigation";
import { connection } from "next/server";
import { Suspense } from "react";
import kit from "@/app/_components/ui/kit.module.css";
import { getMe } from "@/app/(user)/_lib/api";
import { getDemo, getDemoTemplate, getDemoTemplates, getVersionConfig } from "../_lib/api";
import { DemoStudio } from "./_components/DemoStudio";
import type { StudioCode } from "./_lib/studioDocument";
import styles from "./_components/DemoStudio.module.css";

export const metadata: Metadata = { title: "코드로 데모 만들기", robots: { index: false } };

const BLANK: StudioCode = {
  html: "<h1>안녕하세요</h1>\n",
  css: "body { font-family: system-ui, sans-serif; }\n",
  js: "",
  mockConfig: "",
  sampleData: "",
};

function first(value: string | string[] | undefined) {
  return Array.isArray(value) ? value[0] : value;
}

function parseId(value: string | undefined): number | null {
  const id = Number(value);
  return Number.isSafeInteger(id) && id > 0 ? id : null;
}

async function StudioContent({ searchParams }: { searchParams: PageProps<"/demos/studio">["searchParams"] }) {
  await connection();
  const params = await searchParams;
  const me = await getMe();
  if (!me) return <p className={kit.empty}>로그인하면 데모를 만들 수 있어요.</p>;

  const demoId = parseId(first(params.demoId));
  const versionId = parseId(first(params.versionId));
  if (demoId && versionId) {
    const demo = await getDemo(demoId);
    if (!demo || demo.author.id !== me.id) notFound();
    const config = await getVersionConfig(demoId, versionId);
    if (!config) notFound();
    return (
      <DemoStudio
        target={{ demoId, versionId }}
        editable={config.editor !== undefined}
        initial={{
          html: config.editor?.html ?? "",
          css: config.editor?.css ?? "",
          js: config.editor?.js ?? "",
          mockConfig: config.mockConfig ?? "",
          sampleData: config.sampleData ?? "",
        }}
      />
    );
  }

  const key = first(params.template);
  const template = key ? await getDemoTemplate(key) : null;
  if (key && !template) notFound();
  if (template) {
    return (
      <>
        <section className={`${kit.card} ${kit.stack}`} aria-label="템플릿 사용 방법">
          <h2 className={kit.cardTitle}>{template.title}</h2>
          <p className={kit.muted}>{template.usage}</p>
        </section>
        <DemoStudio
          key={template.key}
          target={null}
          editable
          initial={{
            html: template.html,
            css: template.css,
            js: template.js,
            mockConfig: template.mockConfig,
            sampleData: template.sampleData,
          }}
        />
      </>
    );
  }

  const templates = await getDemoTemplates();
  return (
    <>
      <section className={kit.stack} aria-labelledby="studio-templates">
        <h2 id="studio-templates" className={kit.cardTitle}>
          템플릿으로 시작하기
        </h2>
        <ul className={styles.templates}>
          {templates.map((item) => (
            <li key={item.key} className={`${kit.card} ${kit.stack}`}>
              <h3 className={kit.strong}>{item.title}</h3>
              <p className={kit.muted}>{item.description}</p>
              <Link href={`/demos/studio?template=${item.key}`} className={`${kit.btnGhost} ${kit.sm}`}>
                이 템플릿 사용
              </Link>
            </li>
          ))}
        </ul>
      </section>
      <DemoStudio target={null} editable initial={BLANK} />
    </>
  );
}

export default function DemoStudioPage({ searchParams }: PageProps<"/demos/studio">) {
  return (
    <>
      <header>
        <h1 className={kit.pageTitle}>코드로 데모 만들기</h1>
        <p className={kit.pageDesc}>HTML·CSS·JavaScript를 바로 고치고, Mock API와 샘플 데이터로 체험형 데모를 만들어 보세요.</p>
      </header>
      <Suspense fallback={<p role="status" aria-busy="true">불러오는 중…</p>}>
        <StudioContent searchParams={searchParams} />
      </Suspense>
    </>
  );
}
