const gulp = require("gulp");
const sourcemaps = require("gulp-sourcemaps");
const md5 = require("gulp-md5-plus");
const merge = require("merge2");

gulp.task("clean", function() {
    const del = require("del");
    return del(["src/main/dist/web/template", "src/main/dist/web/static/css", "src/main/dist/web/static/js"])
});

gulp.task("html", function() {
    return gulp.src("src/main/web/template/**/*.html")
        .pipe(gulp.dest("src/main/dist/web/template"))
})

gulp.task("css", ["html"], function() {
    const stylelint = require("gulp-stylelint");
    const cssnano = require("gulp-cssnano");

    var appCSS = gulp.src(["src/main/web/static/css/**/*.css", "!src/main/web/static/css/lib{,/**/*.css}"])
        .pipe(sourcemaps.init())
        .pipe(stylelint({
            configFile: "stylelint.json",
            reporters: [{
                formatter: "string",
                console: true
            }]
        }))
        .pipe(cssnano())
        .pipe(md5(10, "src/main/dist/web/template/**/*.html"))
        .pipe(sourcemaps.write("."))
        .pipe(gulp.dest("src/main/dist/web/static/css"));

    var libCSS = gulp.src(["src/main/web/static/css/lib/*.css"])
        .pipe(md5(10, "src/main/dist/web/template/**/*.html"))
        .pipe(gulp.dest("src/main/dist/web/static/css/lib"));

    var resources = gulp.src(["src/main/web/static/css/**/*.*", "!src/main/web/static/css/**/*.css"])
        .pipe(gulp.dest("src/main/dist/web/static/css"));

    return merge(appCSS, libCSS, resources);
});

gulp.task("js", ["html"], function(cb) {
    const uglify = require("gulp-uglify");
    const eslint = require("gulp-eslint");

    var appJS = gulp.src(["src/main/web/static/js/**/*.js", "!src/main/web/static/js/lib{,/**/*.js}"])
        .pipe(eslint({
            configFile: "eslint.json"
        }))
        .pipe(eslint.format())
        .pipe(eslint.failAfterError())
        .pipe(sourcemaps.init())
        .pipe(uglify())
        .pipe(md5(10, "src/main/dist/web/template/**/*.html"))
        .pipe(sourcemaps.write("."))
        .pipe(gulp.dest("src/main/dist/web/static/js"));

    var libJS = gulp.src(["src/main/web/static/js/lib/**/*.js"])
        .pipe(md5(10, "src/main/dist/web/template/**/*.html"))
        .pipe(gulp.dest("src/main/dist/web/static/js/lib"));

    return merge(appJS, libJS);
});

gulp.task("default", ["clean"], function() {
    gulp.start("html", "css", "js");
})

gulp.task("watch", function() {
    gulp.watch(["src/main/web/static/css/**/*.css", "src/main/web/static/js/**/*.js", "src/main/web/template/**/*.html"], ["js", "css"]);
});
