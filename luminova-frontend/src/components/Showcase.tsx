import { cases } from '../data/home';

export function Showcase() {
  return (
    <section className="cases" id="cases">
      <div className="section-row">
        <div>
          <p>Use Cases</p>
          <h2>从创意到成片，团队都在用 Luminova</h2>
        </div>
        <a href="#cases">查看全部案例</a>
      </div>
      <div className="case-grid">
        {cases.map((item) => (
          <article className="case-card" key={item.title}>
            <img src={item.image} alt={item.title} />
            <div>
              <h3>{item.title}</h3>
              <p>{item.desc}</p>
              {item.tags.map((tag) => (
                <span key={tag}>{tag}</span>
              ))}
            </div>
          </article>
        ))}
      </div>
    </section>
  );
}
