import gulp from "gulp";
import md5 from "gulp-md5-plus";
import stylelint from "gulp-stylelint";
import cssnano from "gulp-cssnano";
import eslint from "gulp-eslint";
import sourcemaps from "gulp-sourcemaps";
import babel from "gulp-babel";
import uglify from "gulp-uglify";
import merge from "merge2";
import del from "del";
import { argv } from 'yargs';

const root = `${argv.root}/src/main`;

gulp.task("clean", () => del(`${root}/dist/web`, { force: true }));

gulp.task("resource", () => {
    return gulp.src([`${root}/web/**/*.*`, `!${root}/web/static/css/**/*.css`, `!${root}/web/static/js/**/*.js`])
        .pipe(gulp.dest(`${root}/dist/web`));
});

gulp.task("css", ["resource"], () => {
    const appCSS = gulp.src([`${root}/web/static/css/**/*.css`, `!${root}/web/static/css/lib{,/**/*.css}`])
        .pipe(sourcemaps.init())
        .pipe(stylelint({
            configFile: "stylelint.json", // see https://github.com/stylelint/stylelint-config-standard/blob/master/index.js
            reporters: [{
                formatter: "string",
                console: true
            }]
        }))
        .pipe(cssnano({ zindex: false }))
        .pipe(md5(10, `${root}/dist/web/template/**/*.html`, { dirLevel: 2 }))
        .pipe(sourcemaps.write("."))
        .pipe(gulp.dest(`${root}/dist/web/static/css`));

    const libCSS = gulp.src(`${root}/web/static/css/lib/**/*.css`)
        .pipe(md5(10, `${root}/dist/web/template/**/*.html`, { dirLevel: 2 }))
        .pipe(gulp.dest(`${root}/dist/web/static/css/lib`));

    return merge(appCSS, libCSS);
});

gulp.task("js", ["resource"], () => {
    const appJS = gulp.src([`${root}/web/static/js/**/*.js`, `!${root}/web/static/js/lib{,/**/*.js}`])
        .pipe(eslint({
            configFile: "eslint.json",
            parserOptions: { "ecmaVersion": 2015 }
        }))
        .pipe(eslint.format())
        .pipe(eslint.failAfterError())
        .pipe(sourcemaps.init())
        .pipe(babel({
            presets: ["babel-preset-es2015"].map(require.resolve)
        }))
        .pipe(uglify())
        .pipe(md5(10, `${root}/dist/web/template/**/*.html`, { dirLevel: 2 }))
        .pipe(sourcemaps.write("."))
        .pipe(gulp.dest(`${root}/dist/web/static/js`));

    const libJS = gulp.src([`${root}/web/static/js/lib/**/*.js`])
        .pipe(md5(10, `${root}/dist/web/template/**/*.html`, { dirLevel: 2 }))
        .pipe(gulp.dest(`${root}/dist/web/static/js/lib`));

    return merge(appJS, libJS);
});

gulp.task("build", () => gulp.start("resource", "css", "js"));
