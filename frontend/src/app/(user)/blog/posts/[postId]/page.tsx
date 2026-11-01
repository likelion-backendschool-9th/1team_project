import type { Metadata } from "next";
import { Suspense } from "react";
import { PostView } from "./_components/PostView";

export const metadata: Metadata = { title: "블로그 글" };

export default function PostPage({ params }: PageProps<"/blog/posts/[postId]">) {
  return (
    <>
      <h1 className="sr-only">블로그 글</h1>
      <Suspense fallback={<p role="status" aria-busy="true">불러오는 중…</p>}>
        <PostView params={params} />
      </Suspense>
    </>
  );
}
