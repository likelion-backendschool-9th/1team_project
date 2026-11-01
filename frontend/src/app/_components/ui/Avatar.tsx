import styles from "./Avatar.module.css";

type Props = {
  name: string;
  size?: number;
  tone?: "dark" | "light";
  shape?: "circle" | "square";
};

export function Avatar({ name, size = 40, tone = "dark", shape = "circle" }: Props) {
  return (
    <span
      className={`${styles.avatar} ${tone === "light" ? styles.light : ""} ${shape === "square" ? styles.square : ""}`}
      style={{ width: size, height: size, fontSize: size * 0.4 }}
      aria-hidden="true"
    >
      {name.charAt(0)}
    </span>
  );
}
