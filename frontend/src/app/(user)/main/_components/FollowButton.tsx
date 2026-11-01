"use client";

import { toggleFollow } from "../_actions";
import { useToggle } from "../_hooks/useToggle";
import styles from "./FollowButton.module.css";

type Props = {
  userId: number;
  nickname: string;
  following: boolean;
};

export function FollowButton({ userId, nickname, following }: Props) {
  const follow = useToggle(following, 0, (next) => toggleFollow(userId, next));

  return (
    <>
      <button
        type="button"
        className={`${styles.button} ${follow.active ? styles.following : ""}`}
        aria-pressed={follow.active}
        aria-label={`${nickname} 팔로우`}
        disabled={follow.pending}
        onClick={follow.toggle}
      >
        {follow.active ? "팔로잉" : "팔로우"}
      </button>
      <span role="alert" className="sr-only">
        {follow.error}
      </span>
    </>
  );
}
