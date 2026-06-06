import { showcase } from '../data/home';

export function Showcase() {
  return (
    <section className="showcase" id="cases">
      <div className="section-head">
        <h2>作品预期</h2>
        <p>展示可控工作流带来的分镜预演、广告 Demo 与短剧批量生产能力。</p>
      </div>
      <div className="showcase-grid">
        {showcase.map((item, index) => (
          <article className="showcase-card" key={item.title}>
            <div className={`showcase-art showcase-art--${index + 1}`} />
            <div className="showcase-body">
              <h3>{item.title}</h3>
              <div className="meta">
                {item.tags.map((tag) => (
                  <span className="pill" key={tag}>
                    {tag}
                  </span>
                ))}
              </div>
            </div>
          </article>
        ))}
      </div>
    </section>
  );
}
