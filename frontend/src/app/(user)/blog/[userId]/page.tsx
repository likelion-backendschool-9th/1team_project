import type { Metadata } from "next";
import { Suspense } from "react";
import { AuthorBlog } from "./_components/AuthorBlog";

export const metadata: Metadata = { title: "개발 블로그" };

export default function AuthorBlogPage({ params }: PageProps<"/blog/[userId]">) {
  return (
    <>
      <h1 className="sr-only">개발 블로그</h1>
      <Suspense fallback={<p role="status" aria-busy="true">불러오는 중…</p>}>
        <AuthorBlog params={params} />
      </Suspense>
    </>
  );
}
