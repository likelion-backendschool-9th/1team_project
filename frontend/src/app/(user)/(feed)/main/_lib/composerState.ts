export const MAX_FEED_LENGTH = 500;

export type ComposerState = {
  values: { content: string };
  error: string | null;
  message: string | null;
  attempt: number;
};

export const initialComposerState: ComposerState = {
  values: { content: "" },
  error: null,
  message: null,
  attempt: 0,
};
