import { HeroAgentEntry } from './HeroAgentEntry';
import { HeroPreview } from './HeroPreview';

export function Hero() {
  return (
    <section className="hero" id="product">
      <div className="hero-copy">
        <p className="eyebrow">AI 视频 · 无限画布工作流</p>
        <div className="hero-brand-title" aria-label="Luminova 灵衍">
          <span>Luminova</span>
          <strong>·</strong>
          <em>灵衍</em>
        </div>
        <h1>
          <span>把 AI 视频创作，</span>
          <span>展开成一张可控画布</span>
        </h1>
        <p className="hero-lead">
          剧本、角色、场景、Prompt 与视频版本被组织成可追溯的创作资产，
          团队能看见每一步生成依据。
        </p>
        <HeroAgentEntry />
      </div>
      <HeroPreview />
    </section>
  );
}
