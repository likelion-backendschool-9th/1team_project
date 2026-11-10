/** 홈 카드 방명록 한 줄 입력 최대 길이 (방명록 500자 규칙과 같다) */
export const MAX_COMMENT_LENGTH = 500;

export type CommentState = {
  values: { content: string };
  error: string | null;
  message: string | null;
  attempt: number;
};

export const initialCommentState: CommentState = {
  values: { content: "" },
  error: null,
  message: null,
  attempt: 0,
};
