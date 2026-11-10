"use server";

import { revalidatePath } from "next/cache";
import { redirect } from "next/navigation";
import {
  apiFetch,
  ApiRequestError,
  hasBackend,
  isMockMode,
  type ApiResponse,
} from "@/app/_lib/api/client";
import { runForm } from "@/app/_lib/api/formAction";
import { mutate } from "@/app/_lib/api/mutate";
import { readValues, type FormState } from "@/app/_lib/formState";
import { showcaseLimit } from "@/app/_lib/policy";
import { getMe } from "@/app/(user)/_lib/api";
import { BGM_TRACKS } from "./_lib/bgm";
import { MAX_COMMENT_LENGTH, type CommentState } from "./_lib/commentState";
import { pickRandomMockUserId } from "./_lib/mock";

export type ActionResult = { ok: boolean; message?: string };

function isId(value: number) {
  return Number.isSafeInteger(value) && value > 0;
}

export async function toggleIlchon(userId: number, follow: boolean): Promise<ActionResult> {
  if (!isId(userId)) return { ok: false, message: "잘못된 요청이에요." };
  // 미니홈피 팔로우 버튼. 팔로우 API는 백엔드에 있어서 MOCK_EXCEPT_DM이어도 실서버를 쓴다.
  if (!hasBackend) return { ok: true };

  try {
    await apiFetch(`/api/v1/follows/${userId}`, { method: follow ? "POST" : "DELETE" });
  } catch (error) {
    if (error instanceof ApiRequestError) return { ok: false, message: error.error.message };
    throw error;
  }
  return { ok: true };
}

// 파도타기는 남의 홈피로만 간다. 내 홈피와 지금 보고 있는 홈피는 뽑지 않는다.
export async function surf(currentUserId: number) {
  const me = await getMe();
  let nextId: number;
  if (isMockMode) {
    nextId = pickRandomMockUserId([me?.id ?? 0, isId(currentUserId) ? currentUserId : 0]);
  } else {
    const { data } = await apiFetch<ApiResponse<{ userId: number }>>(
      `/api/v1/mini-homes/random?exclude=${currentUserId}`,
    );
    nextId = data.userId;
  }
  redirect(`/mini-home/${nextId}`);
}

// 홈 카드의 한 줄 방명록. 공개글로 남긴다. 비밀글은 방명록 탭에서 쓴다.
export async function writeComment(
  userId: number,
  previous: CommentState,
  formData: FormData,
): Promise<CommentState> {
  const content = String(formData.get("content") ?? "").trim();
  const attempt = previous.attempt + 1;
  const failed = (error: string): CommentState => ({
    values: { content },
    error,
    message: null,
    attempt,
  });

  if (!isId(userId)) return failed("잘못된 요청이에요.");
  if (!content) return failed("방명록 내용을 입력해 주세요.");
  if (content.length > MAX_COMMENT_LENGTH)
    return failed(`${MAX_COMMENT_LENGTH}자 이하로 입력해 주세요.`);

  if (isMockMode)
    return {
      values: { content: "" },
      error: null,
      message: "백엔드 연결 전이라 저장되지 않았어요.",
      attempt,
    };

  try {
    await apiFetch(`/api/v1/mini-homes/${userId}/guestbook`, {
      method: "POST",
      body: JSON.stringify({ content, secret: false }),
    });
  } catch (error) {
    if (error instanceof ApiRequestError) return failed(error.error.message);
    throw error;
  }
  revalidatePath(`/mini-home/${userId}`);
  revalidatePath(`/mini-home/${userId}/guestbook`);
  return { values: { content: "" }, error: null, message: "방명록을 남겼어요.", attempt };
}

export async function writeGuestbook(
  userId: number,
  previous: FormState,
  formData: FormData,
): Promise<FormState> {
  const values = readValues(formData, ["content"]);
  const secret = formData.get("secret") === "on";
  const errors: Record<string, string> = {};
  if (!values.content) errors.content = "방명록 내용을 입력해 주세요.";
  else if (values.content.length > 500) errors.content = "500자 이하로 입력해 주세요.";

  const state = await runForm(previous, values, errors, () =>
    mutate(`/api/v1/mini-homes/${userId}/guestbook`, "POST", { ...values, secret }, "방명록을 남겼어요."),
  );
  if (state.ok) {
    revalidatePath(`/mini-home/${userId}/guestbook`);
    revalidatePath(`/mini-home/${userId}`);
  }
  return state.ok ? { ...state, values: {} } : state;
}

export async function deleteGuestbook(guestbookId: number) {
  if (!isId(guestbookId)) return { ok: false, message: "잘못된 요청이에요." };
  return mutate(`/api/v1/mini-homes/guestbook/${guestbookId}`, "DELETE", undefined, "방명록을 삭제했어요.");
}

// 백엔드 MiniHomeServiceImpl.DOMAIN과 같은 규칙이다.
const DOMAIN_PATTERN = /^(?=.{4,100}$)([a-z0-9]([a-z0-9-]{0,61}[a-z0-9])?\.)+[a-z]{2,63}$/;

export async function saveMiniHome(previous: FormState, formData: FormData): Promise<FormState> {
  const me = await getMe();
  if (!me) return { ok: false, message: "로그인이 필요해요.", errors: {}, values: {}, attempt: 0 };
  const values = readValues(formData, ["skin", "headline", "domain", "bgm", "quote", "stackTags"]);
  const showcase = formData.getAll("showcaseDemoIds").map(Number).filter(isId);
  const featuredDemoId = Number(formData.get("featuredDemoId"));
  const featuredPostId = Number(formData.get("featuredPostId"));
  values.showcaseDemoIds = showcase.join(",");
  values.featuredPostId = String(featuredPostId || "");
  values.featuredDemoId = String(featuredDemoId || "");
  const errors: Record<string, string> = {};
  if (!values.headline) errors.headline = "한 줄 소개를 입력해 주세요.";
  else if (values.headline.length > 30) errors.headline = "30자 이하로 입력해 주세요.";
  if (values.quote.length > 80) errors.quote = "80자 이하로 입력해 주세요.";
  values.domain = values.domain.trim().toLowerCase().replace(/^https?:\/\//, "").replace(/\/+$/, "");
  if (values.domain && !DOMAIN_PATTERN.test(values.domain))
    errors.domain = "example.dev 같은 형식으로 입력해 주세요.";
  if (showcase.length > showcaseLimit(me.plus))
    errors.showcase = `공개할 데모는 ${showcaseLimit(me.plus)}개까지 고를 수 있어요.`;
  if (isId(featuredDemoId) && !showcase.includes(featuredDemoId))
    errors.showcase = "대표 데모는 공개할 데모 중에서 골라 주세요.";
  // 스킨·BGM은 딥소스 플러스 전용이다.
  if (!me.plus) {
    values.skin = "cream";
    values.bgm = "";
  } else if (values.bgm && !BGM_TRACKS.some((track) => track.id === values.bgm)) {
    errors.bgm = "목록에 있는 곡만 고를 수 있어요.";
  }

  return runForm(previous, values, errors, async () => {
    const body = { ...values, showcaseDemoIds: showcase };
    const saved = await mutate("/api/v1/mini-homes/me", "PUT", body, "미니홈피를 저장했어요.");
    if (!saved.ok) return saved;
    // 제작기 글: 고르지 않았으면 null을 보내 연결을 끊는다.
    const post = await mutate(
      "/api/v1/mini-homes/me/featured-post",
      "PUT",
      { postId: isId(featuredPostId) ? featuredPostId : null },
      "미니홈피를 저장했어요.",
    );
    if (!post.ok) return post;
    if (!isId(featuredDemoId)) return saved;
    const featured = await mutate(
      "/api/v1/mini-homes/me/featured-demo",
      "PUT",
      { demoId: featuredDemoId },
      "미니홈피를 저장했어요.",
    );
    return featured.ok ? saved : featured;
  });
}

// 미니홈피 칩 후원. POST /mini-homes/{userId}/donations { chips } → 204.
// 후원 API는 백엔드에 있어서 MOCK_EXCEPT_DM이어도 실서버를 쓴다.
export async function sponsorChips(userId: number, previous: FormState, formData: FormData): Promise<FormState> {
  const values = readValues(formData, ["amount"]);
  const amount = Number(values.amount);
  const errors: Record<string, string> = {};
  if (!isId(userId)) errors.amount = "잘못된 요청이에요.";
  else if (!Number.isInteger(amount) || amount < 1) errors.amount = "1칩 이상 입력해 주세요.";
  else if (amount > 10_000) errors.amount = "한 번에 10,000칩까지 후원할 수 있어요.";
  return runForm(previous, values, errors, async () => {
    const result = await mutate(
      `/api/v1/mini-homes/${userId}/donations`,
      "POST",
      { chips: amount },
      `${amount}칩을 후원했어요.`,
      !hasBackend,
    );
    if (result.ok && hasBackend) {
      // 줄어든 내 칩 잔액이 바로 보이도록 다시 그린다.
      revalidatePath(`/mini-home/${userId}`);
      revalidatePath("/my/coins");
    }
    return result;
  });
}

export async function expandInventory() {
  if (!isMockMode) return { ok: false, message: "칩 사용 API 연결 전이에요." };
  return { ok: true, message: "인벤토리를 1칸 늘렸어요. (목업이라 실제로 차감되지는 않아요)" };
}

export async function recordVisit(userId: number) {
  if (!isId(userId)) return { ok: false, message: "잘못된 요청이에요." };
  return mutate(`/api/v1/mini-homes/${userId}/visits`, "POST", undefined, "방문을 기록했어요.");
}

export async function toggleDemoLike(demoId: number, like: boolean): Promise<ActionResult> {
  if (!isId(demoId)) return { ok: false, message: "잘못된 요청이에요." };
  return mutate(`/api/v1/demos/${demoId}/like`, like ? "POST" : "DELETE", undefined, "좋아요를 반영했어요.");
}

export async function createDemoComment(
  demoId: number,
  previous: FormState,
  formData: FormData,
): Promise<FormState> {
  const values = readValues(formData, ["content"]);
  const errors: Record<string, string> = {};
  if (!values.content) errors.content = "댓글을 입력해 주세요.";
  else if (values.content.length > 300) errors.content = "300자 이하로 입력해 주세요.";
  const state = await runForm(previous, values, errors, () =>
    mutate(`/api/v1/demos/${demoId}/comments`, "POST", { ...values, type: "praise" }, "댓글을 남겼어요."),
  );
  return state.ok ? { ...state, values: {} } : state;
}

export async function updateDemoComment(commentId: number, content: string) {
  const text = content.trim();
  if (!text || text.length > 300) return { ok: false, message: "1~300자로 입력해 주세요." };
  return mutate(`/api/v1/demos/comments/${commentId}`, "PUT", { content: text }, "댓글을 수정했어요.");
}

export async function deleteDemoComment(commentId: number) {
  return mutate(`/api/v1/demos/comments/${commentId}`, "DELETE", undefined, "댓글을 삭제했어요.");
}

export async function replyDemoComment(demoId: number, parentId: number, content: string) {
  if (!isId(demoId) || !isId(parentId)) return { ok: false, message: "잘못된 요청이에요." };
  const text = content.trim();
  if (!text || text.length > 300) return { ok: false, message: "1~300자로 입력해 주세요." };
  return mutate(`/api/v1/demos/${demoId}/comments`, "POST", { content: text, parentId, type: "praise" }, "답글을 남겼어요.");
}

export async function toggleDemoCommentLike(commentId: number, like: boolean) {
  if (!isId(commentId)) return { ok: false, message: "잘못된 요청이에요." };
  return mutate(`/api/v1/demos/comments/${commentId}/like`, like ? "POST" : "DELETE", undefined, "좋아요를 반영했어요.");
}

export async function reportGuestbook(homeUserId: number, guestbookId: number, reason: string) {
  if (!isId(homeUserId) || !isId(guestbookId)) return { ok: false, message: "잘못된 요청이에요." };
  return mutate(`/api/v1/mini-homes/${homeUserId}/guestbook/${guestbookId}/report`, "POST", { reason }, "신고가 접수됐어요.");
}

export async function reportUser(targetId: number, reason: string) {
  if (!isId(targetId)) return { ok: false, message: "잘못된 요청이에요." };
  return mutate(`/api/v1/users/${targetId}/report`, "POST", { reason }, "신고가 접수됐어요.");
}
