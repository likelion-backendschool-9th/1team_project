export const MAX_FEED_LENGTH = 500;

export type ComposerState = {
  values: { content: string; demoId: string };
  error: string | null;
  message: string | null;
  attempt: number;
};

export const initialComposerState: ComposerState = {
  values: { content: "", demoId: "" },
  error: null,
  message: null,
  attempt: 0,
};
