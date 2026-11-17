/* Charts for the My Applications dashboard (no chart library).
   Each function returns an HTML string so it can be tested with Node. */
(function (root, factory) {
  const lib = factory();
  if (typeof module === 'object' && module.exports) module.exports = lib;
  else root.CCCharts = lib;
})(typeof self !== 'undefined' ? self : this, function () {

  const esc = s => String(s).replace(/[&<>"']/g, c => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));
  const plural = (n, word) => n + ' ' + word + (n === 1 ? '' : 's');

  // Donut chart. The circle's circumference is 100, so each segment's dash length = its percent
  function donut(segments, total) {
    const shown = (segments || []).filter(s => s.count > 0);
    const summary = total
      ? 'Status breakdown of ' + plural(total, 'application') + ': ' +
        shown.map(s => s.label + ' ' + s.count + ' (' + s.percent + '%)').join(', ') + '.'
      : 'Status breakdown: no applications yet.';
    const gap = shown.length > 1 ? 0.8 : 0;      // thin white gap between segments
    let start = 0;
    const arcs = shown.map(s => {
      const share = s.count / total * 100;
      const len = Math.max(share - gap, 0.01);
      const arc = `<circle class="seg" cx="21" cy="21" r="15.9155" stroke="${esc(s.color)}" ` +
        `stroke-dasharray="${len.toFixed(3)} ${(100 - len).toFixed(3)}" stroke-dashoffset="${(25 - start).toFixed(3)}">` +
        `<title>${esc(s.label)}: ${s.count} (${s.percent}%)</title></circle>`;
      start += share;
      return arc;
    }).join('');
    return `<svg class="donut" viewBox="0 0 42 42" role="img" aria-label="${esc(summary)}">` +
      `<circle class="track" cx="21" cy="21" r="15.9155"/>${arcs}` +
      `<text x="21" y="21" class="donut-num" aria-hidden="true">${total || 0}</text>` +
      `<text x="21" y="26.5" class="donut-cap" aria-hidden="true">${total === 1 ? 'application' : 'applications'}</text></svg>`;
  }

  // Legend next to the donut
  function legend(segments) {
    return `<ul class="legend">${(segments || []).map(s =>
      `<li><span class="sw" style="background:${esc(s.color)}" aria-hidden="true"></span>` +
      `<span class="lg-name">${esc(s.label)}</span><b>${s.count}</b><small>${s.percent}%</small></li>`).join('')}</ul>`;
  }

  // Bars per month, scaled to the busiest month. Year shown under the first bar and each January
  function monthBars(months) {
    if (!months || months.length === 0) {
      return '<p class="chart-empty">No dated applications to chart yet.</p>';
    }
    const max = Math.max(1, ...months.map(m => m.count));
    const total = months.reduce((n, m) => n + m.count, 0);
    const label = 'Applications per month from ' + months[0].label + ' to ' + months[months.length - 1].label +
      ': ' + plural(total, 'application') + ' in total.';
    return `<ol class="bars" aria-label="${esc(label)}" style="--n:${months.length}">${months.map((m, i) => {
      const text = m.label + ': ' + plural(m.count, 'application');
      const showYear = i === 0 || m.month === 'Jan';
      return `<li title="${esc(text)}" aria-label="${esc(text)}">` +
        `<span class="bar-val" aria-hidden="true">${m.count > 0 ? m.count : ''}</span>` +
        `<span class="bar${m.count ? '' : ' zero'}" style="--h:${(m.count / max * 100).toFixed(1)}%" aria-hidden="true"></span>` +
        `<span class="bar-lbl" aria-hidden="true">${esc(m.month)}${showYear ? `<small>${m.year}</small>` : ''}</span></li>`;
    }).join('')}</ol>`;
  }

  // Same numbers as a table, for screen readers ("View as table")
  function dataTable(caption, headers, rows) {
    return `<table class="data-table"><caption>${esc(caption)}</caption>` +
      `<thead><tr>${headers.map(h => `<th scope="col">${esc(h)}</th>`).join('')}</tr></thead>` +
      `<tbody>${rows.map(r => `<tr>${r.map((c, i) => i === 0 ? `<th scope="row">${esc(c)}</th>` : `<td>${esc(c)}</td>`).join('')}</tr>`).join('')}</tbody></table>`;
  }

  return { esc, donut, legend, monthBars, dataTable };
});
