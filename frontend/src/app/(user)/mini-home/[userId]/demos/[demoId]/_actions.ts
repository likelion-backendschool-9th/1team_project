'use server';

import { revalidatePath } from 'next/cache';
import { runForm } from '@/app/_lib/api/formAction';
import { redirect } from 'next/navigation';
import { hasBackend } from '@/app/_lib/api/client';
import { mutate } from '@/app/_lib/api/mutate';
import { readValues, type FormState } from '@/app/_lib/formState';

const TYPES = ['question', 'bug', 'praise'];

// 데모 API는 백엔드가 있으므로 MOCK_EXCEPT_DM이어도 실서버를 쓴다.
const send = (
  path: `/api/v1/${string}`,
  method: 'POST' | 'PUT' | 'DELETE',
  body: unknown,
  message: string,
) => mutate(path, method, body, message, !hasBackend);

export async function playDemo(demoId: number) {
  return send(
    `/api/v1/demos/${demoId}/play`,
    'POST',
    undefined,
    '샌드박스를 열었어요.',
  );
}

/** 서버가 최소 간격과 동시 실행 수를 지키고, 한도에 걸리면 마지막 상태를 그대로 돌려준다. */
export async function checkDemoBackendHealth(demoId: number) {
  const result = await send(
    `/api/v1/demos/${demoId}/backend/health-check`,
    'POST',
    undefined,
    '상태를 확인했어요.',
  );
  if (result.ok) revalidatePath('/mini-home/[userId]/demos/[demoId]', 'page');
  return result;
}

export async function toggleDemoLike(demoId: number, like: boolean) {
  return send(
    `/api/v1/demos/${demoId}/like`,
    like ? 'POST' : 'DELETE',
    undefined,
    '좋아요를 반영했어요.',
  );
}

export async function reportDemo(demoId: number, reason: string) {
  return send(
    `/api/v1/demos/${demoId}/report`,
    'POST',
    { reason },
    '신고가 접수되었습니다.',
  );
}

export async function createDemoComment(
  demoId: number,
  previous: FormState,
  formData: FormData,
): Promise<FormState> {
  const values = readValues(formData, ['content', 'type']);
  const errors: Record<string, string> = {};
  if (!values.content) errors.content = '댓글을 입력해 주세요.';
  else if (values.content.length > 300)
    errors.content = '300자 이하로 입력해 주세요.';
  if (!TYPES.includes(values.type)) errors.type = '피드백 종류를 골라 주세요.';

  const state = await runForm(previous, values, errors, () =>
    send(
      `/api/v1/demos/${demoId}/comments`,
      'POST',
      values,
      '피드백을 남겼어요.',
    ),
  );
  if (state.ok) revalidatePath('/mini-home/[userId]/demos/[demoId]', 'page');
  return state.ok ? { ...state, values: { type: values.type } } : state;
}

export async function toggleDemoBookmark(demoId: number, bookmark: boolean) {
  return send(
    `/api/v1/demos/${demoId}/bookmark`,
    bookmark ? 'POST' : 'DELETE',
    undefined,
    bookmark ? '나중에 해보기에 담았어요.' : '나중에 해보기에서 뺐어요.',
  );
}

export async function deleteDemo(demoId: number) {
  const result = await send(
    `/api/v1/demos/${demoId}`,
    'DELETE',
    undefined,
    '데모를 삭제했어요. 인벤토리 1칸이 비었어요.',
  );
  if (result.ok && hasBackend) redirect('/demos');
  return result;
}

export async function updateDemoComment(commentId: number, content: string) {
  const text = content.trim();
  if (!text || text.length > 300)
    return { ok: false, message: '1~300자로 입력해 주세요.' };
  return send(
    `/api/v1/demos/comments/${commentId}`,
    'PUT',
    { content: text },
    '피드백을 수정했어요.',
  );
}

export async function deleteDemoComment(commentId: number) {
  return send(
    `/api/v1/demos/comments/${commentId}`,
    'DELETE',
    undefined,
    '피드백을 삭제했어요.',
  );
}

export async function replyDemoComment(
  demoId: number,
  parentId: number,
  content: string,
) {
  const text = content.trim();
  if (!text || text.length > 300)
    return { ok: false, message: '1~300자로 입력해 주세요.' };
  return send(
    `/api/v1/demos/${demoId}/comments`,
    'POST',
    { content: text, parentId, type: 'praise' },
    '답글을 남겼어요.',
  );
}

export async function toggleDemoCommentLike(commentId: number, like: boolean) {
  return send(
    `/api/v1/demos/comments/${commentId}/like`,
    like ? 'POST' : 'DELETE',
    undefined,
    '좋아요를 반영했어요.',
  );
}
