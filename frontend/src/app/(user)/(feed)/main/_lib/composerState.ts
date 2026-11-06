export const MAX_FEED_LENGTH = 500;
export const MAX_IMAGE_COUNT = 5;

export type ComposerState = {
  values: { content: string; demoId: string; linkUrl: string };
  error: string | null;
  message: string | null;
  attempt: number;
};

export const initialComposerState: ComposerState = {
  values: { content: "", demoId: "", linkUrl: "" },
  error: null,
  message: null,
  attempt: 0,
};
