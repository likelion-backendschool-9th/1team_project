import styles from "./StepHeader.module.css";

type Props = {
  title: string;
  desc: string;
};

export function StepHeader({ title, desc }: Props) {
  return (
    <header>
      <h1 className={styles.title}>{title}</h1>
      <p className={styles.desc}>{desc}</p>
    </header>
  );
}
