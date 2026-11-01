import { getRanking } from "../_lib/api";
import { RankingTabs } from "./RankingTabs";

export async function RankingWidget() {
  const [daily, weekly, monthly] = await Promise.all([
    getRanking("daily"),
    getRanking("weekly"),
    getRanking("monthly"),
  ]);

  return <RankingTabs ranking={{ daily, weekly, monthly }} />;
}
