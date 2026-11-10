export type FormState = {
  ok: boolean | null;
  message: string | null;
  errors: Record<string, string>;
  values: Record<string, string>;
  attempt: number;
};

export const initialFormState: FormState = {
  ok: null,
  message: null,
  errors: {},
  values: {},
  attempt: 0,
};

export function readValues(formData: FormData, keys: string[]): Record<string, string> {
  return Object.fromEntries(keys.map((key) => [key, String(formData.get(key) ?? "").trim()]));
}
