import { logout } from "../_actions";
import styles from "./LogoutButton.module.css";

export function LogoutButton() {
  return (
    <form action={logout} className={styles.form}>
      <button type="submit" className={styles.button}>
        로그아웃
      </button>
    </form>
  );
}
