const gulp = require("gulp");
const sourcemaps = require("gulp-sourcemaps");
const md5 = require("gulp-md5-plus");
const merge = require("merge2");

const argv = require('yargs').argv;
const root = `${argv.root}/src/main`;

gulp.task("clean", () => {
    const del = require("del");
    return del(`${root}/dist/web`, {
        force: true
    });
});

gulp.task("resource", () => {
    return gulp.src([`${root}/web/**/*.*`, `!${root}/web/static/css/**/*.css`, `!${root}/web/static/js/**/*.js`])
        .pipe(gulp.dest(`${root}/dist/web`));
});

gulp.task("css", ["resource"], () => {
    const stylelint = require("gulp-stylelint");
    const cssnano = require("gulp-cssnano");

    const appCSS = gulp.src([`${root}/web/static/css/**/*.css`, `!${root}/web/static/css/lib{,/**/*.css}`])
        .pipe(sourcemaps.init())
        .pipe(stylelint({
            configFile: "stylelint.json", // see https://github.com/stylelint/stylelint-config-standard/blob/master/index.js
            reporters: [{
                formatter: "string",
                console: true
            }]
        }))
        .pipe(cssnano({
            zindex: false
        }))
        .pipe(md5(10, `${root}/dist/web/template/**/*.html`, {
            dirLevel: 2
        }))
        .pipe(sourcemaps.write("."))
        .pipe(gulp.dest(`${root}/dist/web/static/css`));

    const libCSS = gulp.src(`${root}/web/static/css/lib/**/*.css`)
        .pipe(md5(10, `${root}/dist/web/template/**/*.html`, {
            dirLevel: 2
        }))
        .pipe(gulp.dest(`${root}/dist/web/static/css/lib`));

    return merge(appCSS, libCSS);
});

gulp.task("js", ["resource"], cb => {
    const uglify = require("gulp-uglify");
    const eslint = require("gulp-eslint");

    const appJS = gulp.src([`${root}/web/static/js/**/*.js`, `!${root}/web/static/js/lib{,/**/*.js}`])
        .pipe(eslint({
            configFile: "eslint.json"
        }))
        .pipe(eslint.format())
        .pipe(eslint.failAfterError())
        .pipe(sourcemaps.init())
        .pipe(uglify())
        .pipe(md5(10, `${root}/dist/web/template/**/*.html`, {
            dirLevel: 2
        }))
        .pipe(sourcemaps.write("."))
        .pipe(gulp.dest(`${root}/dist/web/static/js`));

    const libJS = gulp.src([`${root}/web/static/js/lib/**/*.js`])
        .pipe(md5(10, `${root}/dist/web/template/**/*.html`, {
            dirLevel: 2
        }))
        .pipe(gulp.dest(`${root}/dist/web/static/js/lib`));

    return merge(appJS, libJS);
});

gulp.task("build", [], () => {
    gulp.start("resource", "css", "js");
});
