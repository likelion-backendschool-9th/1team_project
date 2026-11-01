export const MAX_COMMENT_LENGTH = 100;

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
