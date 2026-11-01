import type { Metadata } from "next";
import { Suspense } from "react";
import kit from "@/app/_components/ui/kit.module.css";
import { createPost } from "../_actions";
import { PostEditor } from "../_components/PostEditor";
import { getDemoOptions } from "../_lib/api";

export const metadata: Metadata = { title: "글쓰기", robots: { index: false } };

async function WriteForm() {
  const demos = await getDemoOptions();
  return <PostEditor action={createPost} demos={demos} submitLabel="발행하기" />;
}

export default function WritePage() {
  return (
    <>
      <header>
        <h1 className={kit.pageTitle}>새 글 쓰기</h1>
        <p className={kit.pageDesc}>마크다운으로 쓰고, 데모를 연결하거나 유료 글로 발행할 수 있어요.</p>
      </header>
      <Suspense fallback={<p role="status" aria-busy="true">불러오는 중…</p>}>
        <WriteForm />
      </Suspense>
    </>
  );
}
