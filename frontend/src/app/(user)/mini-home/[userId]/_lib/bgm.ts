// public/bgm/에 넣어 둔 음원을 고르는 방식이다 (설정 정리 2026-11-02).
// 지금 파일은 팀에서 코드로 직접 합성한 25~35초 반복 루프(싸이월드 감성으로 악기 통일)라 저작권 문제가 없다.
// 다른 음원으로 바꿀 때는 같은 파일 이름으로 덮어쓰면 되고, 출처·라이선스를 확인한다.
export const BGM_TRACKS = [
  { id: "lofi-coding", title: "Lofi Coding Night", src: "/bgm/lofi-coding.mp3" },
  { id: "rainy-commit", title: "Rainy Day Commit", src: "/bgm/rainy-commit.mp3" },
  { id: "deploy-friday", title: "Deploy on Friday", src: "/bgm/deploy-friday.mp3" },
];

export function findTrack(id: string | null) {
  return BGM_TRACKS.find((track) => track.id === id) ?? null;
}
