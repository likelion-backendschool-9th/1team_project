import type { Metadata } from "next";
import kit from "@/app/_components/ui/kit.module.css";
import { UploadForm } from "./_components/UploadForm";

export const metadata: Metadata = { title: "데모 올리기", robots: { index: false } };

export default function DemoUploadPage() {
  return (
    <>
      <header>
        <h1 className={kit.pageTitle}>데모 올리기</h1>
        <p className={kit.pageDesc}>
          zip 파일 하나로 올리면 브라우저 샌드박스에서 바로 실행돼요. html/css/js 단일 구동과 Unity
          WebGL 빌드를 지원합니다.
        </p>
      </header>
      <UploadForm />
    </>
  );
}
