"use client";

import { useRouter } from "next/navigation";
import { useRef, useState, useTransition } from "react";
import { Avatar } from "@/app/_components/ui/Avatar";
import kit from "@/app/_components/ui/kit.module.css";
import styles from "./My.module.css";

type Props = {
  nickname: string;
  profileImage: string | null;
};

const MAX_BYTES = 10 * 1024 * 1024; // 백엔드 ImageService와 같은 10MB
const ACCEPT = "image/jpeg,image/png,image/gif,image/webp";

// 파일은 Server Action(기본 1MB 제한)을 거치지 않고 브라우저에서 바로 /api/v1로 보낸다.
// next.config의 리라이트가 백엔드로 넘겨 주고, 로그인 쿠키도 같이 간다.
async function request(method: "POST" | "DELETE", body?: FormData): Promise<string | null> {
  const res = await fetch("/api/v1/users/me/profile-image", { method, body });
  if (res.ok) return null;
  if (res.status === 401) return "로그인이 만료됐어요. 새로고침한 뒤 다시 시도해 주세요.";
  const data = await res.json().catch(() => null);
  return data?.error?.message ?? "프로필 이미지를 바꾸지 못했어요.";
}

export function ProfileImageUploader({ nickname, profileImage }: Props) {
  const router = useRouter();
  const inputRef = useRef<HTMLInputElement>(null);
  const [message, setMessage] = useState<string | null>(null);
  const [pending, startTransition] = useTransition();

  function run(task: () => Promise<string | null>) {
    setMessage(null);
    startTransition(async () => {
      const error = await task();
      if (error) {
        setMessage(error);
        return;
      }
      // 서버에서 내 정보를 다시 읽어 와서 프로필 카드·피드의 사진까지 같이 바뀐다
      router.refresh();
    });
  }

  function handleChange(event: React.ChangeEvent<HTMLInputElement>) {
    const file = event.target.files?.[0];
    event.target.value = ""; // 같은 파일을 다시 골라도 onChange가 불리게 비운다
    if (!file) return;
    if (file.size > MAX_BYTES) {
      setMessage("10MB 이하 이미지만 올릴 수 있어요.");
      return;
    }
    const body = new FormData();
    body.append("file", file); // 백엔드 @RequestPart("file")와 이름이 같아야 한다
    run(() => request("POST", body));
  }

  return (
    <div className={styles.avatarEdit}>
      <Avatar name={nickname} src={profileImage} size={72} />
      <div className={kit.row}>
        <button
          type="button"
          className={`${kit.btn} ${kit.sm}`}
          onClick={() => inputRef.current?.click()}
          disabled={pending}
        >
          {pending ? "저장 중…" : "사진 바꾸기"}
        </button>
        {profileImage && (
          <button
            type="button"
            className={`${kit.btnGhost} ${kit.sm}`}
            onClick={() => run(() => request("DELETE"))}
            disabled={pending}
          >
            기본 이미지로
          </button>
        )}
      </div>
      <input ref={inputRef} type="file" accept={ACCEPT} onChange={handleChange} hidden />
      {message && (
        <p role="alert" className={kit.error}>
          {message}
        </p>
      )}
    </div>
  );
}