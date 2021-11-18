// copy the element text to clipboard for the given element id
function copyToClipboard(id) {
    const copyText = document.getElementById(id).innerText;
    navigator.clipboard.writeText(copyText);
}

function addSliderForSvgZoom() {
    d3.selectAll("svg")
        .each(function () {
            let svg = d3.select(this);
            let svgParent = d3.select(this.parentNode);
            let viewBox = svg.attr("viewBox");
            let viewBoxDimensions = viewBox.split(" ");
            let width = viewBoxDimensions[2];
            let height = viewBoxDimensions[3];
            svgParent.insert('input', 'svg')
                .attr("min", 0.1)
                .attr("type", "range")
                .attr("max", 1)
                .attr("step", 0.01)
                .attr("value", 1)
                .on("input", function () {
                    let sliderValue = this.value;
                    let newWidth = width / sliderValue;
                    let newHeight = height / sliderValue;
                    let newViewBox = '0 0 ' + newWidth + ' ' + newHeight;
                    svg.attr("viewBox", newViewBox)
                    svg.attr("style", "") // remove inline styling which overrides height and width
                    svg.attr("width", width * sliderValue)
                    svg.attr("height", height * sliderValue)
                });
        });
}

function highlightLifelinesWhenClicked() {
    d3.selectAll("svg")
        .each(function () {
            d3.select(this).selectAll('line')
                .filter(function () {
                    let currentLine = d3.select(this);
                    return currentLine.attr("x1") === currentLine.attr("x2"); // only vertical lifelines
                })
                .each(function () {
                    let currentLine = d3.select(this);
                    let originalStyle = currentLine.attr("style")
                    let clickedStyle = originalStyle + "stroke-width:4.0;"
                    let toggle = true
                    currentLine.on("click", function () {
                        let style = toggle ? clickedStyle : originalStyle
                        toggle = !toggle
                        currentLine.attr("style", style)
                    })
                })
        });
}

const keywordOptions = {
    "element": "span",
    "className": "keyword"
};

// Add a span with class: "keyword" around matching keywords so that they can be styled
function highlightKeywords() {
    const patterns = [/Given/, /When/, /Then/, /And/];
    let keywordMarker = new Mark(document.querySelectorAll("section.description"));
    patterns.forEach(value => keywordMarker.markRegExp(value, keywordOptions));
}

const factOptions = {
    "element": "span",
    "className": "highlight",
    "separateWordSearch": false,
    "acrossElements": true,
    "accuracy": {
        "value": "exactly", "limiters": [",", ".", ";", ":", "-", "(", ")", "<", ">", "/"]
    }
}

function highlightFact(id, word) {
    let factMarker = new Mark(document.getElementById(id));
    factMarker.mark(word, factOptions);
}

<!-- Remove highlights in svg (causes text to disappear) -->
function unMarkSvg() {
    let unmarker = new Mark(document.querySelectorAll("svg"));
    unmarker.unmark();
}
