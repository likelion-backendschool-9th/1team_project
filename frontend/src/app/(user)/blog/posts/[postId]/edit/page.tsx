import type { Metadata } from "next";
import Link from "next/link";
import { notFound } from "next/navigation";
import { connection } from "next/server";
import { Suspense } from "react";
import kit from "@/app/_components/ui/kit.module.css";
import { getMe } from "@/app/(user)/_lib/api";
import { updatePost } from "../../../_actions";
import { PostEditor } from "../../../_components/PostEditor";
import { getDemoOptions, getPost } from "../../../_lib/api";

export const metadata: Metadata = { title: "글 수정", robots: { index: false } };

async function EditForm({ params }: { params: Promise<{ postId: string }> }) {
  await connection();
  const postId = Number((await params).postId);
  const post = Number.isSafeInteger(postId) && postId > 0 ? await getPost(postId) : null;
  if (!post) notFound();
  const [me, demos] = await Promise.all([getMe(), getDemoOptions()]);

  if (!me || me.id !== post.author.id)
    return (
      <p className={`${kit.card} ${kit.empty}`}>
        본인이 쓴 글만 수정할 수 있어요.{" "}
        <Link href={`/blog/posts/${post.id}`} className={kit.more}>
          글로 돌아가기
        </Link>
      </p>
    );

  return (
    <PostEditor
      action={updatePost.bind(null, post.id)}
      demos={demos}
      submitLabel="수정 완료"
      draftKey={`edit-${post.id}`}
      initial={{
        title: post.title,
        category: post.category,
        tags: post.tags.join(", "),
        content: post.content,
        demoId: post.demo ? String(post.demo.id) : "",
        price: String(post.price || 10),
        paid: post.price > 0,
      }}
    />
  );
}

export default function EditPostPage({ params }: PageProps<"/blog/posts/[postId]/edit">) {
  return (
    <>
      <h1 className={kit.pageTitle}>글 수정</h1>
      <Suspense fallback={<p role="status" aria-busy="true">불러오는 중…</p>}>
        <EditForm params={params} />
      </Suspense>
    </>
  );
}
