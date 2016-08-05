var gulp = require('gulp');
var sourcemaps = require('gulp-sourcemaps');
var md5 = require("gulp-md5-plus");
var merge = require('merge2');

gulp.task('clean', function() {
    var del = require('del');
    return del(['src/main/dist/web/template', 'src/main/dist/web/static/css', 'src/main/dist/web/static/js'])
});

gulp.task('html', function() {
    return gulp.src('src/main/web/template/**/*.html')
        .pipe(gulp.dest('src/main/dist/web/template'))
})

gulp.task("css", ["html"], function() {
    var postcss = require('gulp-postcss');
    var stylelint = require('stylelint');
    var reporter = require('postcss-reporter');
    var autoprefixer = require('autoprefixer');
    var cssnano = require('cssnano');

    var processors = [
        require('precss'),
        stylelint({
            configFile: 'stylelint.json'
        }),
        reporter({
            clearMessages: true,
            throwError: true
        }),
        autoprefixer(),
        cssnano()
    ];

    var appCSS = gulp.src(['src/main/web/static/css/**/*.css', '!src/main/web/static/css/vendor{,/**/*.css}'])
        .pipe(sourcemaps.init())
        .pipe(postcss(processors))
        .pipe(md5(10, 'src/main/dist/web/template/**/*.html'))
        .pipe(sourcemaps.write('.'))
        .pipe(gulp.dest('src/main/dist/web/static/css'));

    var vendorCSS = gulp.src(['src/main/web/static/css/vendor{,/**/*.css}'])
        .pipe(md5(10, 'src/main/dist/web/template/**/*.html'))
        .pipe(gulp.dest('src/main/dist/web/static/css/vendor'));

    return merge(appCSS, vendorCSS);
});

gulp.task('js', ["html"], function(cb) {
    var uglify = require('gulp-uglify');
    var eslint = require('gulp-eslint');

    var appJS = gulp.src(['src/main/web/static/js/**/*.js', '!src/main/web/static/js/vendor{,/**/*.js}'])
        .pipe(eslint({
            configFile: 'eslint.json'
        }))
        .pipe(eslint.format())
        .pipe(eslint.failAfterError())
        .pipe(sourcemaps.init())
        .pipe(uglify())
        .pipe(md5(10, 'src/main/dist/web/template/**/*.html'))
        .pipe(sourcemaps.write('.'))
        .pipe(gulp.dest('src/main/dist/web/static/js'));

    var vendorJS = gulp.src(['src/main/web/static/js/vendor/**/*.js'])
        .pipe(md5(10, 'src/main/dist/web/template/**/*.html'))
        .pipe(gulp.dest('src/main/dist/web/static/js/vendor'));

    return merge(appJS, vendorJS);
});

gulp.task('default', ['clean'], function() {
    gulp.start('html', 'css', 'js');
})

gulp.task('watch', function() {
    gulp.watch(['src/main/web/static/css/**/*.css', 'src/main/web/static/js/**/*.js', 'src/main/web/template/**/*.html'], ['js', 'css']);
});