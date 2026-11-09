'use server';

import { revalidatePath } from 'next/cache';
import { runForm } from '@/app/_lib/api/formAction';
import { mutate } from '@/app/_lib/api/mutate';
import { readValues, type FormState } from '@/app/_lib/formState';

const TYPES = ['question', 'bug', 'praise'];

export async function playDemo(demoId: number) {
  return mutate(
    `/api/v1/demos/${demoId}/play`,
    'POST',
    undefined,
    '샌드박스를 열었어요.',
  );
}

export async function toggleDemoLike(demoId: number, like: boolean) {
  return mutate(
    `/api/v1/demos/${demoId}/like`,
    like ? 'POST' : 'DELETE',
    undefined,
    '좋아요를 반영했어요.',
  );
}

export async function reportDemo(demoId: number, reason: string) {
  return mutate(
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
    mutate(
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
  return mutate(
    `/api/v1/demos/${demoId}/bookmark`,
    bookmark ? 'POST' : 'DELETE',
    undefined,
    bookmark ? '나중에 해보기에 담았어요.' : '나중에 해보기에서 뺐어요.',
  );
}

export async function deleteDemo(demoId: number) {
  return mutate(
    `/api/v1/demos/${demoId}`,
    'DELETE',
    undefined,
    '데모를 삭제했어요. 인벤토리 1칸이 비었어요.',
  );
}

export async function updateDemoComment(commentId: number, content: string) {
  const text = content.trim();
  if (!text || text.length > 300)
    return { ok: false, message: '1~300자로 입력해 주세요.' };
  return mutate(
    `/api/v1/demos/comments/${commentId}`,
    'PUT',
    { content: text },
    '피드백을 수정했어요.',
  );
}

export async function deleteDemoComment(commentId: number) {
  return mutate(
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
  return mutate(
    `/api/v1/demos/${demoId}/comments`,
    'POST',
    { content: text, parentId, type: 'praise' },
    '답글을 남겼어요.',
  );
}

export async function toggleDemoCommentLike(commentId: number, like: boolean) {
  return mutate(
    `/api/v1/demos/comments/${commentId}/like`,
    like ? 'POST' : 'DELETE',
    undefined,
    '좋아요를 반영했어요.',
  );
}
