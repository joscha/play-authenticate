#!/bin/sh
find . -type f \( ! -name ".*" \) -exec echo "<a href=\"{}\">{}</a><br/>" ";" > index.html