import styles from "./Avatar.module.css";

type Props = {
  name: string;
  size?: number;
  tone?: "dark" | "light";
};

export function Avatar({ name, size = 40, tone = "dark" }: Props) {
  return (
    <span
      className={`${styles.avatar} ${tone === "light" ? styles.light : ""}`}
      style={{ width: size, height: size, fontSize: size * 0.4 }}
      aria-hidden="true"
    >
      {name.charAt(0)}
    </span>
  );
}
