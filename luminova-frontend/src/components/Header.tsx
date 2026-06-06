export function Header() {
  return (
    <header className="shell-nav">
      <a className="brand" href="/" aria-label="Luminova 首页">
        <img className="brand-logo" src="/luminova-logo-hifi.png" alt="" />
        <span className="brand-word">
          <b>Luminova</b>
          <strong>·</strong>
          <em>灵衍</em>
        </span>
      </a>
      <nav className="nav" aria-label="主导航">
        <a className="active" href="/">
          首页
        </a>
        <a href="#workflow">工作空间</a>
        <a href="#cases">画布</a>
      </nav>
      <div className="actions">
        <button className="btn" type="button">
          登录
        </button>
        <button className="btn primary" type="button">
          开始创作
        </button>
      </div>
    </header>
  );
}
