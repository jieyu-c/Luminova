import { useEffect, useState } from "react";
import { ArrowRight, Play, X } from "@phosphor-icons/react";

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
  const [menuOpen, setMenuOpen] = useState(false);

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
          <button className="login-button" type="button" onClick={() => setAuthOpen(true)}>登录</button>
          <button className="menu-button" type="button" aria-label="展开导航" aria-expanded={menuOpen} onClick={() => setMenuOpen((open) => !open)}>
            <span />
            <span />
          </button>
        </div>
      </header>

      <main id="top">
        <section className="hero" id="product" aria-labelledby="hero-title">
          <div className="hero-copy">
            <p className="eyebrow">AI VIDEO DIRECTION · 2026</p>
            <h1 id="hero-title">把想象，<span>执导成片</span></h1>
            <div className="hero-subline" aria-hidden="true">
              <i />
              <p>你的判断始终在创作中心</p>
              <i />
            </div>
          </div>

          <figure className="cinematic-frame" id="work">
            <img src="/assets/luminova-homepage-cinematic-triptych.png" alt="女性创作者、雾中山岭与胶片放映机组成的电影画面" />
            <figcaption><span>THE FIRST LIGHT</span><span>FILM STUDY · 01</span></figcaption>
          </figure>

          <div className="hero-actions">
            <button className="primary-cta" type="button" onClick={() => setAuthOpen(true)}>
              开始一部新作品 <ArrowRight aria-hidden="true" weight="light" />
            </button>
            <button className="secondary-cta" type="button" onClick={() => scrollToSection("method")}>
              <Play aria-hidden="true" weight="fill" /> 了解导演 Agent
            </button>
          </div>
          <p className="trust-line">每次执行前展示计划，由你决定是否继续</p>
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
          <button type="button" onClick={() => setAuthOpen(true)}>进入 Luminova <ArrowRight aria-hidden="true" weight="light" /></button>
        </section>
      </main>

      <footer className="site-footer">
        <div className="footer-brand">Luminova 灵衍</div>
        <p>AI VIDEO CREATION, DIRECTED BY YOU.</p>
        <p>© 2026 LUMINOVA</p>
      </footer>

      {authOpen && (
        <div className="modal-backdrop" role="presentation" onMouseDown={() => setAuthOpen(false)}>
          <section className="auth-dialog" role="dialog" aria-modal="true" aria-labelledby="auth-title" onMouseDown={(event) => event.stopPropagation()}>
            <button className="dialog-close" type="button" aria-label="关闭" onClick={() => setAuthOpen(false)}><X aria-hidden="true" weight="light" /></button>
            <p className="dialog-kicker">WELCOME TO LUMINOVA</p>
            <h2 id="auth-title">让第一部作品开始流动</h2>
            <p>登录后创建画布、整理创作条目，并继续最近打开的作品。</p>
            <div className="dialog-actions"><button type="button">手机号登录</button><button type="button">创建账号</button></div>
            <small>认证页面将在下一阶段接入现有登录与注册接口。</small>
          </section>
        </div>
      )}
    </div>
  );
}
