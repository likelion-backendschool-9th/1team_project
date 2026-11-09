import kit from "@/app/_components/ui/kit.module.css";
import { MAX_BACKEND_URL } from "../upload/_lib/rules";

type Props = {
  idPrefix: string;
  errors: Record<string, string | undefined>;
  apiBaseUrl?: string;
  healthCheckUrl?: string;
};

/** 외부 백엔드 주소 입력. 등록과 관리 화면이 함께 쓴다. */
export function BackendFields({ idPrefix, errors, apiBaseUrl, healthCheckUrl }: Props) {
  const invalid = (key: string) =>
    errors[key] ? { "aria-invalid": true as const, "aria-describedby": `${idPrefix}-${key}-error` } : {};

  return (
    <>
      <div className={kit.field}>
        <label htmlFor={`${idPrefix}-apiBaseUrl`} className={kit.label}>
          API Base URL
        </label>
        <input
          id={`${idPrefix}-apiBaseUrl`}
          name="apiBaseUrl"
          type="url"
          inputMode="url"
          maxLength={MAX_BACKEND_URL}
          defaultValue={apiBaseUrl}
          placeholder="https://example.com/api"
          className={kit.input}
          {...invalid("apiBaseUrl")}
        />
        <p id={`${idPrefix}-apiBaseUrl-error`} className={kit.error}>
          {errors.apiBaseUrl}
        </p>
      </div>
      <div className={kit.field}>
        <label htmlFor={`${idPrefix}-healthCheckUrl`} className={kit.label}>
          상태 확인 주소 (선택)
        </label>
        <input
          id={`${idPrefix}-healthCheckUrl`}
          name="healthCheckUrl"
          type="url"
          inputMode="url"
          maxLength={MAX_BACKEND_URL}
          defaultValue={healthCheckUrl}
          placeholder="https://example.com/health"
          className={kit.input}
          {...invalid("healthCheckUrl")}
        />
        <p id={`${idPrefix}-healthCheckUrl-error`} className={kit.error}>
          {errors.healthCheckUrl}
        </p>
      </div>
      <ul className={kit.hint}>
        <li>찍먹은 주소만 저장하고, 백엔드는 직접 배포한 서버에서 돌아요. API 키는 넣지 마세요.</li>
        <li>백엔드 CORS에서 Origin: null(또는 *)을 허용해야 해요. 샌드박스에서는 쿠키를 쓸 수 없어요.</li>
        <li>
          zip 안의 API 주소를 자동으로 바꾸지는 않아요. 이 주소로 빌드해서 올리거나, 코드에서{" "}
          <code>window.DIP_DEMO_ENV?.API_BASE_URL</code>을 읽어 주세요.
        </li>
      </ul>
    </>
  );
}
