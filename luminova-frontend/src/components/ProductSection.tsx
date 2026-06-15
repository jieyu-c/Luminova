import { ProductShowcase } from './ProductShowcase';

export function ProductSection() {
  return (
    <section className="product-section" id="product-preview" aria-label="产品工作流展示">
      <div className="section-heading section-heading--left">
        <p>Product Preview</p>
        <h2>从灵感输入到视频版本，每一步都能被看见</h2>
      </div>
      <ProductShowcase />
    </section>
  );
}
