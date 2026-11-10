import type { Metadata } from "next";
import { Suspense } from "react";
import kit from "@/app/_components/ui/kit.module.css";
import { inventoryCapacity } from "@/app/_lib/policy";
import { getMe } from "@/app/(user)/_lib/api";
import { countMyDemos, getDemoUsage } from "../_lib/api";
import { UploadForm } from "./_components/UploadForm";

export const metadata: Metadata = { title: "데모 올리기", robots: { index: false } };

async function UploadContent() {
  const me = await getMe();
  if (!me) return null;
  const [used, usage] = await Promise.all([countMyDemos(me.id), getDemoUsage()]);
  return <UploadForm used={used} capacity={inventoryCapacity(me.plus, me.extraInventorySlots ?? 0)} usage={usage} />;
}

export default function DemoUploadPage() {
  return (
    <>
      <header>
        <h1 className={kit.pageTitle}>데모 올리기</h1>
        <p className={kit.pageDesc}>
          빌드한 정적 파일을 zip으로 올리면 브라우저 샌드박스에서 바로 실행돼요. HTML/CSS/JS와 React·Vue
          빌드 결과물을 지원합니다.
        </p>
      </header>
      <Suspense fallback={<p role="status" aria-busy="true">불러오는 중…</p>}>
        <UploadContent />
      </Suspense>
    </>
  );
}
