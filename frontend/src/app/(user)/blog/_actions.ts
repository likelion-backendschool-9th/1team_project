'use server';

import { revalidatePath } from "next/cache";
import { redirect } from "next/navigation";
import { apiFetch, ApiRequestError, hasBackend, type ApiResponse } from "@/app/_lib/api/client";
import { runForm } from "@/app/_lib/api/formAction";
import { mutate } from "@/app/_lib/api/mutate";
import { readValues, type FormState } from "@/app/_lib/formState";
import type { PostComment } from "@/app/_lib/types/post";
import { categories, PAID_MIN_LENGTH } from "./_lib/query";

const POST_FIELDS = ['title', 'category', 'tags', 'content', 'demoId', 'price'];

function isId(value: number) {
  return Number.isSafeInteger(value) && value > 0;
}

function validatePost(formData: FormData) {
  const values = readValues(formData, POST_FIELDS);
  const paid = formData.get('paid') === 'on';
  values.paid = paid ? 'on' : '';
  const errors: Record<string, string> = {};
  if (!values.title) errors.title = '제목을 입력해 주세요.';
  else if (values.title.length > 80)
    errors.title = '80자 이하로 입력해 주세요.';
  if (!categories.some((item) => item.value === values.category))
    errors.category = '분류를 골라 주세요.';
  if (values.content.length < 20)
    errors.content = '본문을 20자 이상 입력해 주세요.';
  else if (paid && values.content.length < PAID_MIN_LENGTH)
    errors.content = `유료 글은 본문을 ${PAID_MIN_LENGTH.toLocaleString()}자 이상 써 주세요. (지금 ${values.content.length.toLocaleString()}자)`;
  const price = Number(values.price);
  if (paid && (!Number.isInteger(price) || price < 1 || price > 100))
    errors.price = '1~100칩 사이로 입력해 주세요.';
  const body = {
    title: values.title,
    category: values.category,
    tags: values.tags
      .split(',')
      .map((tag) => tag.trim())
      .filter(Boolean),
    content: values.content,
    demoId: Number(values.demoId) || null,
    price: paid ? price : 0,
  };
  return { values, errors, body };
}

export async function createPost(
  previous: FormState,
  formData: FormData,
): Promise<FormState> {
  const { values, errors, body } = validatePost(formData);
  const attempt = previous.attempt + 1;
  if (Object.keys(errors).length > 0)
    return {
      ok: false,
      message: '입력값을 확인해 주세요.',
      errors,
      values,
      attempt,
    };

  // 글 CRUD는 백엔드에 있어서 MOCK_EXCEPT_DM이어도 실서버를 쓴다.
  if (!hasBackend)
    return {
      ok: true,
      message: '글을 발행했어요. (백엔드 연결 전이라 실제로 저장되지는 않아요)',
      errors: {},
      values,
      attempt,
    };

  let postId: number;
  try {
    const { data } = await apiFetch<ApiResponse<{ id: number }>>(
      '/api/v1/posts',
      {
        method: 'POST',
        body: JSON.stringify(body),
      },
    );
    postId = data.id;
  } catch (error) {
    if (error instanceof ApiRequestError)
      return {
        ok: false,
        message: error.error.message,
        errors: {},
        values,
        attempt,
      };
    throw error;
  }
  revalidatePath('/blog');
  redirect(`/blog/posts/${postId}?created=1`);
}

export async function updatePost(
  postId: number,
  previous: FormState,
  formData: FormData,
): Promise<FormState> {
  const { values, errors, body } = validatePost(formData);
  const state = await runForm(previous, values, errors, () =>
    mutate(
      `/api/v1/posts/${postId}`,
      'PUT',
      body,
      '글을 수정했어요.',
      !hasBackend,
    ),
  );
  if (!state.ok || !hasBackend) return state;
  revalidatePath(`/blog/posts/${postId}`);
  redirect(`/blog/posts/${postId}?updated=1`);
}

export async function deletePost(postId: number) {
  if (!isId(postId)) return { ok: false, message: '잘못된 요청이에요.' };
  const result = await mutate(
    `/api/v1/posts/${postId}`,
    'DELETE',
    undefined,
    '글을 삭제했어요.',
    !hasBackend,
  );
  if (!result.ok || !hasBackend) return result;
  // 삭제한 글 화면에 남지 않도록 블로그 목록으로 보낸다.
  revalidatePath('/blog');
  redirect('/blog?deleted=1');
}

export async function reportPost(postId: number, reason: string) {
  if (!isId(postId)) return { ok: false, message: '잘못된 요청이에요.' };
  return mutate(
    `/api/v1/posts/${postId}/report`,
    'POST',
    { reason },
    '신고가 접수되었습니다.',
    // 신고 API는 백엔드에 있어서 MOCK_EXCEPT_DM이어도 실서버를 쓴다.
    !hasBackend,
  );
}

// 유료 글 칩 구매: POST /posts/{postId}/purchase
export async function unlockPost(postId: number) {
  if (!isId(postId)) return { ok: false, message: '잘못된 요청이에요.' };
  // 칩 사용 API는 백엔드에 있어서 MOCK_EXCEPT_DM이어도 실서버를 쓴다.
  const result = await mutate(
    `/api/v1/posts/${postId}/purchase`,
    'POST',
    undefined,
    '잠금을 해제했어요.',
    !hasBackend,
  );
  if (!result.ok || !hasBackend) return result;
  // 본문 전체와 줄어든 칩 잔액이 바로 보이도록 다시 그린다.
  // 잔액은 레이아웃(오른쪽 프로필 카드)에도 보여서 레이아웃째로 다시 그린다 (글 상세·/my/coins 포함).
  revalidatePath('/', 'layout');
  return result;
}

export async function subscribeBlog(userId: number) {
  if (!isId(userId)) return { ok: false, message: '잘못된 요청이에요.' };
  // 구독 API는 백엔드에 있어서 MOCK_EXCEPT_DM이어도 실서버를 쓴다.
  const result = await mutate(
    '/api/v1/subscriptions',
    'POST',
    { authorId: userId },
    '구독했어요. 매달 같은 날 자동이체돼요.',
    !hasBackend,
  );
  if (!result.ok || !hasBackend) return result;
  // 구독 버튼·유료 글 잠금이 바로 바뀌도록 다시 그린다.
  revalidatePath(`/blog/${userId}`);
  revalidatePath('/blog/posts/[postId]', 'page');
  revalidatePath('/my/subscription');
  return result;
}

export async function createPostComment(
  postId: number,
  previous: FormState,
  formData: FormData,
): Promise<FormState> {
  const values = readValues(formData, ['content']);
  const errors: Record<string, string> = {};
  if (!values.content) errors.content = "댓글을 입력해 주세요.";
  else if (values.content.length > 1000) errors.content = "1000자 이하로 입력해 주세요.";
  const state = await runForm(previous, values, errors, () =>
    mutate(
      `/api/v1/posts/${postId}/comments`,
      'POST',
      values,
      '댓글을 남겼어요.',
      !hasBackend,
    ),
  );
  if (state.ok) revalidatePath(`/blog/posts/${postId}`);
  return state.ok ? { ...state, values: {} } : state;
}

export async function togglePostLike(postId: number, like: boolean) {
  if (!isId(postId)) return { ok: false, message: '잘못된 요청이에요.' };
  return mutate(
    `/api/v1/posts/${postId}/like`,
    like ? 'POST' : 'DELETE',
    undefined,
    like ? '좋아요를 반영했어요.' : '좋아요를 취소했어요.',
    !hasBackend,
  );
}

export async function togglePostBookmark(postId: number, bookmark: boolean) {
  if (!isId(postId)) return { ok: false, message: '잘못된 요청이에요.' };
  return mutate(
    `/api/v1/posts/${postId}/bookmark`,
    bookmark ? 'POST' : 'DELETE',
    undefined,
    bookmark ? '북마크했어요.' : '북마크를 해제했어요.',
    // 북마크 API는 백엔드에 있어서 MOCK_EXCEPT_DM이어도 실서버를 쓴다.
    !hasBackend,
  );
}

export async function updatePostComment(commentId: number, content: string) {
  const text = content.trim();
  if (!text || text.length > 1000) return { ok: false, message: "1~1000자로 입력해 주세요." };
  return mutate(`/api/v1/posts/comments/${commentId}`, "PUT", { content: text }, "댓글을 수정했어요.", !hasBackend);
}

export async function deletePostComment(postId: number, commentId: number) {
  if (!isId(postId) || !isId(commentId)) return { ok: false, message: "잘못된 요청이에요." };
  const result = await mutate(`/api/v1/posts/comments/${commentId}`, "DELETE", undefined, "댓글을 삭제했어요.", !hasBackend);
  if (result.ok) revalidatePath(`/blog/posts/${postId}`);
  return result;
}

export async function replyPostComment(postId: number, parentId: number, content: string) {
  if (!isId(postId) || !isId(parentId)) return { ok: false, message: "잘못된 요청이에요.", data: null };
  const text = content.trim();
  if (!text || text.length > 1000) return { ok: false, message: "1~1000자로 입력해 주세요.", data: null };
  // 백엔드가 없을 때만 저장 없이 화면에서만 처리한다 (댓글 API는 백엔드에 있어서 MOCK_EXCEPT_DM이어도 실서버를 쓴다)
  if (!hasBackend) return { ok: true, message: "답글을 남겼어요.", data: null };
  try {
    const { data } = await apiFetch<ApiResponse<PostComment>>(
      `/api/v1/posts/${postId}/comments`,
      { method: "POST", body: JSON.stringify({ content: text, parentId }) },
    );
    revalidatePath(`/blog/posts/${postId}`);
    return { ok: true, message: "답글을 남겼어요.", data };
  } catch (error) {
    if (error instanceof ApiRequestError) return { ok: false, message: error.error.message, data: null };
    throw error;
  }
}

export async function loadMorePostComments(postId: number, cursor: string) {
  if (!isId(postId)) return { ok: false, message: "잘못된 요청이에요.", comments: [] as PostComment[], hasNext: false, nextCursor: null as string | null };
  try {
    const { data } = await apiFetch<ApiResponse<{ content: PostComment[]; hasNext: boolean; nextCursor: string | null }>>(
      `/api/v1/posts/${postId}/comments?cursor=${cursor}`,
    );
    return { ok: true, message: "", comments: data.content, hasNext: data.hasNext, nextCursor: data.nextCursor };
  } catch (error) {
    if (error instanceof ApiRequestError) return { ok: false, message: error.error.message, comments: [] as PostComment[], hasNext: false, nextCursor: null as string | null };
    throw error;
  }
}

export async function togglePostCommentLike(commentId: number, like: boolean) {
  if (!isId(commentId)) return { ok: false, message: '잘못된 요청이에요.' };
  return mutate(
    `/api/v1/posts/comments/${commentId}/like`,
    like ? 'POST' : 'DELETE',
    undefined,
    '좋아요를 반영했어요.',
    !hasBackend,
  );
}
