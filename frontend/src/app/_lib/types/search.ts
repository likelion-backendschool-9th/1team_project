/** 검색어 자동완성 후보. 백엔드 AutocompleteResponse와 같다 */
export type AutocompleteSuggestion = {
  /** 검색창에 채울 검색어 */
  text: string;
  /** TAG: 태그 이름, WORD: 공개 제목·태그에 나오는 단어, TERM: 기술 용어 사전, CORRECTION: 자판 보정 후보 */
  type: "TAG" | "WORD" | "TERM" | "CORRECTION";
};
