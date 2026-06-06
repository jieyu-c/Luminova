import { highlights } from '../data/home';

export function PromoStrip() {
  return (
    <section className="promo-strip" aria-label="产品亮点">
      {highlights.map((item, index) => (
        <article
          className={`signal-card promo-card glass ${index === 2 ? 'promo-card--accent' : ''}`}
          key={item.label}
        >
          <span className="promo-kicker">{item.label}</span>
          <h3>{item.title}</h3>
          <p>{item.text}</p>
        </article>
      ))}
    </section>
  );
}
