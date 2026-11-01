// 저작권 없는 무료 음원을 public/bgm/에 넣어 두고 고르는 방식이다 (설정 정리 2026-11-02).
// 실제 파일은 아직 없다. 파일을 넣으면 그대로 재생된다.
export const BGM_TRACKS = [
  { id: "lofi-coding", title: "Lofi Coding Night", src: "/bgm/lofi-coding.mp3" },
  { id: "pixel-morning", title: "8-bit Pixel Morning", src: "/bgm/pixel-morning.mp3" },
  { id: "rainy-commit", title: "Rainy Day Commit", src: "/bgm/rainy-commit.mp3" },
  { id: "deploy-friday", title: "Deploy on Friday", src: "/bgm/deploy-friday.mp3" },
];

export function findTrack(id: string | null) {
  return BGM_TRACKS.find((track) => track.id === id) ?? null;
}
