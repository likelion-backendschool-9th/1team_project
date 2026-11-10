import styles from "./Avatar.module.css";

type Props = {
  name: string;
  /** 프로필 이미지 주소. 없으면 이름 첫 글자를 보여 준다 */
  src?: string | null;
  size?: number;
  tone?: "dark" | "light";
  shape?: "circle" | "square";
};

export function Avatar({ name, src, size = 40, tone = "dark", shape = "circle" }: Props) {
  return (
    <span
      className={`${styles.avatar} ${tone === "light" ? styles.light : ""} ${shape === "square" ? styles.square : ""}`}
      style={{ width: size, height: size, fontSize: size * 0.4 }}
      aria-hidden="true"
    >
      {src ? (
        // 소셜·R2 등 여러 곳의 주소라 next/image 대신 img를 쓴다. 옆에 닉네임이 함께 나와서 alt는 비운다
        // eslint-disable-next-line @next/next/no-img-element
        <img src={src} alt="" className={styles.image} loading="lazy" referrerPolicy="no-referrer" />
      ) : (
        name.charAt(0)
      )}
    </span>
  );
}