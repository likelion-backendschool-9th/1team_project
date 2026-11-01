import { mockUsers } from "@/app/(user)/_lib/mock";
import type { DmMessage, DmRoom } from "@/app/_lib/types/dm";

const MINUTE = 60_000;

export function createMockRooms(now: number): DmRoom[] {
  const at = (minutes: number) => new Date(now - minutes * MINUTE).toISOString();
  return [
    { user: mockUsers.cha, online: true, lastMessage: "채팅앱 재연결 로직 코드 공유드릴게요!", lastAt: at(3), unreadCount: 2 },
    { user: mockUsers.kwak, online: true, lastMessage: "글래스모피즘 컴포넌트 써도 된대요 ㅎㅎ", lastAt: at(40), unreadCount: 0 },
    { user: mockUsers.jung, online: false, lastMessage: "파티클 데모 소스 정리되면 알려드릴게요", lastAt: at(180), unreadCount: 1 },
    { user: mockUsers.seungjo, online: false, lastMessage: "오목 게임 버그 제보 감사합니다 🙏", lastAt: at(60 * 26), unreadCount: 0 },
    { user: mockUsers.jinyeop, online: false, lastMessage: "다음 스터디 때 봬요!", lastAt: at(60 * 72), unreadCount: 0 },
  ];
}

export function createMockMessages(myId: number, partnerId: number, now: number): DmMessage[] {
  const lines: [boolean, string, number][] = [
    [false, "안녕하세요! 피드에 올리신 채팅앱 데모 잘 봤어요.", 50],
    [true, "앗 감사합니다 😊 혹시 어떤 부분이 궁금하셨어요?", 47],
    [false, "네트워크가 끊겼다가 다시 붙을 때 메시지 유실이 없던데 어떻게 처리하셨나요?", 45],
    [true, "마지막으로 받은 메시지 id를 들고 재접속하면 서버가 그 뒤 메시지를 다시 보내줘요.", 41],
    [true, "SockJS가 자동 재연결을 안 해줘서 지수 백오프로 직접 붙였어요.", 40],
    [false, "오 깔끔하네요. 코드 일부만 공유 가능할까요?", 5],
    [false, "채팅앱 재연결 로직 코드 공유드릴게요!", 3],
  ];
  return lines.map(([mine, content, minutesAgo], index) => ({
    id: partnerId * 1000 + index,
    senderId: mine ? myId : partnerId,
    content,
    createdAt: new Date(now - minutesAgo * MINUTE).toISOString(),
    read: minutesAgo > 4,
  }));
}
