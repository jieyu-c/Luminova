import { FormEvent, useEffect, useState } from "react";
import { ArrowRight, CircleNotch, Play, X } from "@phosphor-icons/react";

const navItems = [
  { label: "产品", target: "product" },
  { label: "作品", target: "work" },
  { label: "方法", target: "method" },
  { label: "关于", target: "about" },
];

const principles = [
  { index: "01", title: "先有表达，再有生成", description: "从故事意图出发，让每一个镜头都服务于创作判断。" },
  { index: "02", title: "Agent 提案，你来决定", description: "所有关键操作先展示计划，创作者始终保留最终控制权。" },
  { index: "03", title: "从灵感走到成片", description: "把故事、角色、镜头与版本收纳进同一条创作脉络。" },
];

function scrollToSection(target: string) {
  document.getElementById(target)?.scrollIntoView({ behavior: "smooth", block: "start" });
}

/**
 * 官网首页。
 *
 * 页面只负责产品表达与登录转化，不承载工作台或画布业务状态。开始创作与登录
 * 统一进入认证提示层，后续接入认证路由时无需改变首页信息结构。
 */
export function App() {
  const [authOpen, setAuthOpen] = useState(false);
  const [authMode, setAuthMode] = useState<"login" | "register">("login");
  const [loginMethod, setLoginMethod] = useState<"code" | "password">("code");
  const [authError, setAuthError] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [agreed, setAgreed] = useState(false);
  const [menuOpen, setMenuOpen] = useState(false);

  const openAuth = (mode: "login" | "register" = "login") => {
    setAuthMode(mode);
    setAuthError("");
    setAuthOpen(true);
  };

  const switchAuthMode = (mode: "login" | "register") => {
    setAuthMode(mode);
    setAuthError("");
    setIsSubmitting(false);
  };

  const handleAuthSubmit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    const data = new FormData(event.currentTarget);
    const phone = String(data.get("phone") ?? "").trim();
    const credentialName = authMode === "register" ? "code" : loginMethod === "password" ? "password" : "code";
    const credential = String(data.get(credentialName) ?? "").trim();
    const newPassword = String(data.get("password") ?? "");

    if (!/^1\d{10}$/.test(phone)) {
      setAuthError("请输入正确的 11 位手机号");
      return;
    }
    if (!credential) {
      setAuthError(authMode === "login" && loginMethod === "password" ? "请输入登录密码" : "请输入 6 位验证码");
      return;
    }
    if (authMode === "register" && newPassword.length < 8) {
      setAuthError("密码至少需要 8 位字符");
      return;
    }
    if (authMode === "register" && !agreed) {
      setAuthError("请先阅读并同意服务协议与隐私政策");
      return;
    }

    setAuthError("");
    setIsSubmitting(true);
    window.setTimeout(() => setIsSubmitting(false), 900);
  };

  useEffect(() => {
    if (!authOpen) return;

    const handleEscape = (event: KeyboardEvent) => {
      if (event.key === "Escape") setAuthOpen(false);
    };

    document.body.classList.add("modal-open");
    window.addEventListener("keydown", handleEscape);
    return () => {
      document.body.classList.remove("modal-open");
      window.removeEventListener("keydown", handleEscape);
    };
  }, [authOpen]);

  return (
    <div className="site-shell">
      <header className="site-header" aria-label="主导航">
        <a className="brand" href="#top" aria-label="Luminova 灵衍首页">
          <span className="brand-latin">Luminova</span>
          <span className="brand-cn">灵衍</span>
        </a>
        <nav className={menuOpen ? "nav-links is-open" : "nav-links"}>
          {navItems.map((item) => (
            <button key={item.target} type="button" onClick={() => {
              scrollToSection(item.target);
              setMenuOpen(false);
            }}>
              {item.label}
            </button>
          ))}
        </nav>
        <div className="header-actions">
          <button className="login-button" type="button" onClick={() => openAuth("login")}>登录</button>
          <button className="menu-button" type="button" aria-label="展开导航" aria-expanded={menuOpen} onClick={() => setMenuOpen((open) => !open)}>
            <span />
            <span />
          </button>
        </div>
      </header>

      <main id="top">
        <section className="hero" id="product" aria-labelledby="hero-title">
          <div className="hero-reel" id="work" aria-label="五段斜切电影画面组成的长卷缓慢向左循环移动">
            <div className="film-track" aria-hidden="true">
              <img src="/assets/luminova-film-montage-five-wide.png" alt="" />
              <img src="/assets/luminova-film-montage-five-wide.png" alt="" />
            </div>
            <p className="film-caption"><span>FIVE SCENES · ONE FILM</span><span>36S · CONTINUOUS LOOP</span></p>
          </div>

          <div className="hero-shade" aria-hidden="true" />
          <div className="hero-content">
            <div className="hero-copy">
              <p className="eyebrow">AI VIDEO DIRECTION · 2026</p>
              <h1 id="hero-title">把想象，<span>执导成片</span></h1>
              <div className="hero-subline" aria-hidden="true">
                <i />
                <p>你的判断始终在创作中心</p>
                <i />
              </div>
            </div>

            <div className="hero-actions">
              <button className="primary-cta" type="button" onClick={() => openAuth("register")}>
                开始一部新作品 <ArrowRight aria-hidden="true" weight="light" />
              </button>
              <button className="secondary-cta" type="button" onClick={() => scrollToSection("method")}>
                <Play aria-hidden="true" weight="fill" /> 了解导演 Agent
              </button>
            </div>
            <p className="trust-line">每次执行前展示计划，由你决定是否继续</p>
          </div>

          <div className="film-progress" aria-hidden="true">
            <span>00:00</span><i /><span>00:36</span>
          </div>
        </section>

        <section className="method-section" id="method" aria-labelledby="method-title">
          <div className="section-heading">
            <p>HOW WE CREATE</p>
            <h2 id="method-title">创作不是一次点击，<br />而是一连串清醒的选择。</h2>
          </div>
          <div className="principle-list">
            {principles.map((principle) => (
              <article key={principle.index} className="principle">
                <span>{principle.index}</span>
                <div><h3>{principle.title}</h3><p>{principle.description}</p></div>
              </article>
            ))}
          </div>
        </section>

        <section className="about-section" id="about">
          <p className="about-kicker">LUMINOVA · 灵衍</p>
          <p className="about-statement">为专业创作者打造的 AI 视频创作空间。<br />Agent 负责拓展可能，你负责判断什么值得被留下。</p>
          <button type="button" onClick={() => openAuth("register")}>进入 Luminova <ArrowRight aria-hidden="true" weight="light" /></button>
        </section>
      </main>

      <footer className="site-footer">
        <div className="footer-brand">Luminova 灵衍</div>
        <p>AI VIDEO CREATION, DIRECTED BY YOU.</p>
        <p>© 2026 LUMINOVA</p>
      </footer>

      {authOpen && (
        <div className="modal-backdrop" role="presentation" onMouseDown={() => setAuthOpen(false)}>
          <section className={`auth-dialog is-${authMode}`} role="dialog" aria-modal="true" aria-labelledby="auth-title" onMouseDown={(event) => event.stopPropagation()}>
            <button className="dialog-close" type="button" aria-label="关闭" onClick={() => setAuthOpen(false)}><X aria-hidden="true" weight="light" /></button>
            <div className="auth-heading">
              <p className="dialog-kicker">{authMode === "login" ? "WELCOME BACK" : "BEGIN YOUR STORY"}</p>
              <h2 id="auth-title">{authMode === "login" ? "继续你的创作" : "创建创作者账号"}</h2>
            </div>

            <div className="auth-tabs" role="tablist" aria-label="登录或注册">
              <button className={authMode === "login" ? "is-active" : ""} type="button" role="tab" aria-selected={authMode === "login"} onClick={() => switchAuthMode("login")}>登录</button>
              <button className={authMode === "register" ? "is-active" : ""} type="button" role="tab" aria-selected={authMode === "register"} onClick={() => switchAuthMode("register")}>注册</button>
            </div>

            <form className="auth-form" onSubmit={handleAuthSubmit} noValidate>
              <label className="auth-field">
                <span>手机号</span>
                <input name="phone" type="tel" inputMode="numeric" autoComplete="tel" placeholder="请输入手机号" aria-invalid={Boolean(authError)} />
              </label>

              {loginMethod === "code" || authMode === "register" ? (
                <label className="auth-field">
                  <span>验证码</span>
                  <div className="auth-input-action">
                    <input name="code" type="text" inputMode="numeric" autoComplete="one-time-code" maxLength={6} placeholder="6 位验证码" />
                    <button type="button">获取验证码</button>
                  </div>
                </label>
              ) : (
                <label className="auth-field">
                  <span>密码</span>
                  <input name="password" type="password" autoComplete="current-password" placeholder="请输入登录密码" />
                </label>
              )}

              {authMode === "register" && (
                <label className="auth-field">
                  <span>设置密码</span>
                  <input name="password" type="password" autoComplete="new-password" minLength={8} placeholder="至少 8 位字符" />
                </label>
              )}

              {authError && <p className="auth-error" role="alert">{authError}</p>}

              <button className="auth-submit" type="submit" disabled={isSubmitting}>
                {isSubmitting ? <><CircleNotch aria-hidden="true" className="spin" /> 处理中...</> : <>{authMode === "login" ? "登录并继续" : "创建账号并开始"}<ArrowRight aria-hidden="true" /></>}
              </button>

              {authMode === "login" ? (
                <>
                  <div className="auth-divider"><i /><span>或</span><i /></div>
                  <button className="auth-secondary" type="button" onClick={() => {
                    setLoginMethod((method) => method === "code" ? "password" : "code");
                    setAuthError("");
                  }}>{loginMethod === "code" ? "使用密码登录" : "使用验证码登录"}</button>
                  <p className="auth-switch">首次来到灵衍？ <button type="button" onClick={() => switchAuthMode("register")}>创建账号</button></p>
                </>
              ) : (
                <>
                  <label className="auth-consent" id="terms">
                    <input type="checkbox" checked={agreed} onChange={(event) => setAgreed(event.target.checked)} />
                    <span>我已阅读并同意 <a href="#terms">服务协议</a> 与 <a id="privacy" href="#privacy">隐私政策</a></span>
                  </label>
                  <p className="auth-switch">已有账号？ <button type="button" onClick={() => switchAuthMode("login")}>返回登录</button></p>
                </>
              )}
            </form>
          </section>
        </div>
      )}
    </div>
  );
}
