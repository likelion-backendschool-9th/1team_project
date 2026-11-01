import kit from "./kit.module.css";

type Props = {
  ok: boolean | null;
  message: string | null;
};

// 폼 결과 안내. live region은 항상 DOM에 두고 내용만 바꾼다.
export function FormMessage({ ok, message }: Props) {
  return (
    <>
      <p role="status" className={kit.success}>
        {ok ? message : null}
      </p>
      <p role="alert" className={kit.error}>
        {ok === false ? message : null}
      </p>
    </>
  );
}
