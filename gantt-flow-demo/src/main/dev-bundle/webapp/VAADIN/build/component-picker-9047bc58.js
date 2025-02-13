import{L as a,c as r,h as l,b as u,p as g,e as m,q as f}from"./indexhtml-f8137023.js";import{c as v}from"./custom-element-1c6555a1.js";function y(e){var o;const t=[];for(;e&&e.parentNode;){const s=w(e);if(s.nodeId!==-1){if((o=s.element)!=null&&o.tagName.startsWith("FLOW-CONTAINER-"))break;t.push(s)}e=e.parentElement?e.parentElement:e.parentNode.host}return t.reverse()}function w(e){const t=window.Vaadin;if(t&&t.Flow){const{clients:o}=t.Flow,s=Object.keys(o);for(const i of s){const n=o[i];if(n.getNodeId){const c=n.getNodeId(e);if(c>=0)return{nodeId:c,uiId:n.getUIId(),element:e}}}}return{nodeId:-1,uiId:-1,element:void 0}}var E=Object.defineProperty,k=Object.getOwnPropertyDescriptor,C=(e,t,o,s)=>{for(var i=s>1?void 0:s?k(t,o):t,n=e.length-1,c;n>=0;n--)(c=e[n])&&(i=(s?c(t,o,i):c(i))||i);return s&&i&&E(t,o,i),i};let d=class extends a{render(){return l`<div
      tabindex="-1"
      @mousemove=${this.onMouseMove}
      @click=${this.onClick}
      @keydown=${this.onKeyDown}
    ></div>`}onClick(e){const t=this.getTargetElement(e);this.dispatchEvent(new CustomEvent("shim-click",{detail:{target:t}}))}onMouseMove(e){const t=this.getTargetElement(e);this.dispatchEvent(new CustomEvent("shim-mousemove",{detail:{target:t}}))}onKeyDown(e){this.dispatchEvent(new CustomEvent("shim-keydown",{detail:{originalEvent:e}}))}getTargetElement(e){this.style.display="none";const t=document.elementFromPoint(e.clientX,e.clientY);return this.style.display="",t}};d.shadowRootOptions={...a.shadowRootOptions,delegatesFocus:!0};d.styles=[r`
      div {
        pointer-events: auto;
        background: rgba(255, 255, 255, 0);
        position: fixed;
        inset: 0px;
        z-index: 1000000;
      }
    `];d=C([v("vaadin-dev-tools-shim")],d);var b=Object.defineProperty,I=Object.getOwnPropertyDescriptor,p=(e,t,o,s)=>{for(var i=s>1?void 0:s?I(t,o):t,n=e.length-1,c;n>=0;n--)(c=e[n])&&(i=(s?c(t,o,i):c(i))||i);return s&&i&&b(t,o,i),i};let h=class extends a{constructor(){super(...arguments),this.active=!1,this.components=[],this.selected=0}connectedCallback(){super.connectedCallback();const e=new CSSStyleSheet;e.replaceSync(`
    .vaadin-dev-tools-highlight {
      outline: 1px solid red
    }`),document.adoptedStyleSheets=[...document.adoptedStyleSheets,e]}render(){return this.style.display=this.active?"block":"none",l`
      <vaadin-dev-tools-shim
        @shim-click=${this.shimClick}
        @shim-mousemove=${this.shimMove}
        @shim-keydown=${this.shimKeydown}
      ></vaadin-dev-tools-shim>
      <div class="window popup component-picker-info">
        <div>
          <h3>Locate a component in source code</h3>
          <p>Use the mouse cursor to highligh components in the UI.</p>
          <p>Use arrow down/up to cycle through and highlight specific components under the cursor.</p>
          <p>
            Click the primary mouse button to open the corresponding source code line of the highlighted component in
            your IDE.
          </p>
        </div>
      </div>
      <div class="window popup component-picker-components-info">
        <div>
          ${this.components.map((e,t)=>l`<div class=${t===this.selected?"selected":""}>
                ${e.element.tagName.toLowerCase()}
              </div>`)}
        </div>
      </div>
    `}update(e){var t;if(super.update(e),(e.has("selected")||e.has("components"))&&this.highlight((t=this.components[this.selected])==null?void 0:t.element),e.has("active")){const o=e.get("active"),s=this.active;!o&&s?requestAnimationFrame(()=>this.shim.focus()):o&&!s&&this.highlight(void 0)}}shimKeydown(e){const t=e.detail.originalEvent;if(t.key==="Escape")this.abort(),e.stopPropagation(),e.preventDefault();else if(t.key==="ArrowUp"){let o=this.selected-1;o<0&&(o=this.components.length-1),this.selected=o}else t.key==="ArrowDown"?this.selected=(this.selected+1)%this.components.length:t.key==="Enter"&&(this.pickSelectedComponent(),e.stopPropagation(),e.preventDefault())}shimMove(e){const t=e.detail.target;this.components=y(t),this.selected=this.components.length-1}shimClick(e){this.pickSelectedComponent()}abort(){this.dispatchEvent(new CustomEvent("component-picker-abort",{}))}pickSelectedComponent(){const e=this.components[this.selected];if(!e){this.abort();return}this.dispatchEvent(new CustomEvent("component-picker-pick",{detail:{component:{nodeId:e.nodeId,uiId:e.uiId}}}))}highlight(e){this.highlighted&&this.highlighted.classList.remove("vaadin-dev-tools-highlight"),this.highlighted=e,this.highlighted&&this.highlighted.classList.add("vaadin-dev-tools-highlight")}};h.styles=[u,r`
      .component-picker-info {
        left: 1em;
        bottom: 1em;
      }

      .component-picker-components-info {
        right: 3em;
        bottom: 1em;
      }

      .component-picker-components-info .selected {
        font-weight: bold;
      }
    `];p([g({type:Boolean})],h.prototype,"active",2);p([m()],h.prototype,"components",2);p([m()],h.prototype,"selected",2);p([f("vaadin-dev-tools-shim")],h.prototype,"shim",2);h=p([v("vaadin-dev-tools-component-picker")],h);export{h as ComponentPicker};
